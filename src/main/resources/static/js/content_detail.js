document.addEventListener('DOMContentLoaded', function () {
    // Thymeleaf를 통해 HTML <script> 블록에 인라인으로 주입된 'contentDetail' 전역 객체를 사용합니다.
    // 이 객체에는 서버로부터 전달된 관광지 상세 정보가 담겨 있습니다.

    // --- 데이터 추출 및 초기 변수 설정 ---
    const contentId = contentDetail ? contentDetail.contentId : null;             // 콘텐츠 ID
    const contentTypeId = contentDetail ? contentDetail.contentTypeId : null;     // 콘텐츠 타입 ID
    const mapX = contentDetail ? contentDetail.mapX : null;                       // X 좌표 (경도)
    const mapY = contentDetail ? contentDetail.mapY : null;                       // Y 좌표 (위도)
    const placeTitle = contentDetail ? contentDetail.title : '선택 위치';         // 장소명 (지도 마커용)
    const placeAddress = contentDetail ? contentDetail.addr1 : '';                // 주소 (지도 마커용)
    const homepageData = contentDetail ? contentDetail.homepage : null;           // 홈페이지 정보 (HTML 문자열 또는 URL)
    const mainFirstImage = contentDetail ? contentDetail.firstImage : null;       // 대표 이미지 URL
    const mainFirstImage2 = contentDetail ? contentDetail.firstImage2 : null;     // 보조 대표 이미지 URL
    const additionalImagesData = contentDetail && contentDetail.images ? contentDetail.images : []; // 추가 이미지 목록
    const infosData = contentDetail && contentDetail.infos ? contentDetail.infos : [];             // 상세 정보 목록 (텍스트 기반)
    const roomData = contentDetail && contentDetail.rooms ? contentDetail.rooms : [];               // 객실 정보 목록 (숙박시설용)
    const introDetailsMap = contentDetail && contentDetail.introDetails && contentDetail.introDetails.introDetails ? contentDetail.introDetails.introDetails : {}; // 소개 정보 (타입별 키-값 쌍)
    const petTourInfoData = contentDetail && contentDetail.petTourInfo ? contentDetail.petTourInfo : null; // 반려동물 동반 정보

    // HTML 요소 가져오기
    const homepageLinkElement = document.getElementById('dynamicHomepageLink'); // 홈페이지 링크 <a> 태그

    // --- 디버깅용 로그 (배포 시 주석 처리 또는 제거 권장) ---
    console.log("콘텐츠 상세 정보 객체 (contentDetail):", contentDetail);
    console.log("소개 정보 맵 (introDetailsMap):", introDetailsMap);
    console.log("추가 이미지 목록 (additionalImagesData):", additionalImagesData);
    console.log("상세 정보 목록 (infosData):", infosData);
    console.log("객실 정보 목록 (roomData):", roomData);
    console.log("반려동물 동반 정보 (petTourInfoData):", petTourInfoData);

    // --- 핵심 로직 실행 조건 검사 ---
    // contentId가 없으면 페이지를 정상적으로 표시할 수 없으므로, 사용자에게 알리고 함수 실행을 중단합니다.
    if (!contentId) {
        console.warn("Content ID를 사용할 수 없습니다. 상세 페이지가 정상적으로 동작하지 않을 수 있습니다.");
        const loadingSpinner = document.getElementById('pageLoadingSpinner');
        if (loadingSpinner) loadingSpinner.style.display = 'none'; // 로딩 스피너 숨김

        // contentDetail 객체 자체가 null인 경우, 콘텐츠 영역에 직접 메시지를 표시합니다.
        const contentArea = document.querySelector('.content-detail-page');
        if (contentArea) {
            contentArea.innerHTML = '<p class="alert alert-warning">콘텐츠 정보를 불러오는데 실패했습니다. 페이지를 새로고침하거나 다시 시도해주세요.</p>';
        }
        return; // 함수 실행 중단
    }

    // --- 홈페이지 링크 동적 처리 ---
    // homepageData (문자열)에서 실제 URL과 링크 텍스트를 추출하여 <a> 태그를 설정합니다.
    if (homepageLinkElement && homepageData) {
        let extractedUrl = null;
        // 홈페이지 정보가 HTML <a> 태그를 포함하고 있는지 검사하여 href 속성 값을 추출 시도
        const hrefMatch = homepageData.match(/<a\s+(?:[^>]*?\s+)?href="([^"]*)"/i);
        if (hrefMatch && hrefMatch[1]) {
            extractedUrl = hrefMatch[1];
        } else if (homepageData.toLowerCase().startsWith('http') || homepageData.toLowerCase().startsWith('www')) {
            // <a> 태그가 없지만, 문자열 자체가 URL 형태인 경우
            extractedUrl = homepageData;
        }

        if (extractedUrl) {
            // 추출된 URL이 'http://' 또는 'https://'로 시작하지 않으면 'http://'를 붙여줍니다. (상대 경로 방지)
            if (!/^https?:\/\//i.test(extractedUrl)) {
                extractedUrl = 'http://' + extractedUrl;
            }
            homepageLinkElement.href = extractedUrl;
            // homepageLinkElement.querySelector('span').textContent = "새로운 링크 텍스트"; // 필요시 링크 텍스트도 변경
            homepageLinkElement.style.display = ''; // 링크 버튼을 보이도록 설정
        } else {
            homepageLinkElement.style.display = 'none'; // 유효한 URL을 찾지 못하면 링크 버튼 숨김
        }
    } else if (homepageLinkElement) {
        homepageLinkElement.style.display = 'none'; // homepageData가 없으면 링크 버튼 숨김
    }

    // --- 주요 기능 함수 호출 ---
    loadThumbnails(mainFirstImage, mainFirstImage2, additionalImagesData); // 썸네일 이미지 로드 및 표시
    renderIntroDetails(introDetailsMap); // "이용 정보" 섹션 렌더링
    initializeTabsAndFirstContent(contentTypeId, mapX, mapY, infosData, petTourInfoData, roomData, placeTitle, placeAddress); // 정보 탭 초기화 및 첫 번째 탭 내용 로드

    // 초기 로딩 스피너가 있다면 숨깁니다.
    const initialSpinner = document.getElementById('pageLoadingSpinnerContainer'); // 스피너 컨테이너 ID로 변경
    if (initialSpinner) initialSpinner.style.display = 'none';
});


/**
 * API 응답 키(영문)를 화면에 표시할 한글 레이블로 변환하기 위한 매핑 객체입니다.
 * introKeyToLabelMap에 정의되지 않은 키는 formatIntroKeyFallback 함수를 통해 변환 시도됩니다.
 */
const introKeyToLabelMap = {
    // 공통 및 기존 항목 (예시, 필요에 따라 확장)
    "infocenter": "문의 및 안내", "restdate": "쉬는날", "parking": "주차시설", "usetime": "이용시간",
    "chkcreditcard": "신용카드 사용", "chkpet": "반려동물 동반", "chkbabycarriage": "유모차 대여",
    "accomcount": "수용인원", "expagerange": "체험 가능 연령", "expguide": "체험 안내", "opendate": "개장일",
    "useseason": "이용시기", "heritage1": "세계 문화유산", "heritage2": "세계 자연유산", "heritage3": "세계 기록유산",

    // contentTypeId: 12 (관광지)
    // (공통 항목 외 추가될 수 있는 관광지 특화 키들을 여기에 정의)

    // contentTypeId: 14 (문화시설)
    "scale": "규모", "usefee": "이용요금", "spendtime": "관람 소요시간", "parkingfee": "주차요금",
    "discountinfo": "할인정보", "parkingculture": "주차시설(문화)", "usetimeculture": "이용시간(문화)",
    "restdateculture": "쉬는날(문화)", "accomcountculture": "수용인원(문화)", "infocenterculture": "문의 및 안내(문화)",
    "chkcreditcardculture": "신용카드(문화)",

    // contentTypeId: 28 (레포츠)
    "openperiod": "개장기간", "reservation": "예약안내", "scaleleports": "규모(레포츠)",
    "usefeeleports": "이용요금(레포츠)", "parkingleports": "주차시설(레포츠)", "usetimeleports": "이용시간(레포츠)",
    "restdateleports": "쉬는날(레포츠)", "accomcountleports": "수용인원(레포츠)", "infocenterleports": "문의 및 안내(레포츠)",
    "parkingfeeleports": "주차요금(레포츠)", "expagerangeleports": "체험 가능 연령(레포츠)",
    "chkcreditcardleports": "신용카드(레포츠)", "chkpetleports": "반려동물 동반(레포츠)",

    // contentTypeId: 32 (숙박)
    "hanok": "한옥 여부", "sauna": "사우나 시설", "beauty": "뷰티시설", "pickup": "픽업서비스",
    "sports": "스포츠 시설", "benikia": "베니키아 여부", "bicycle": "자전거 대여", "fitness": "피트니스 센터",
    "karaoke": "노래방 시설", "seminar": "세미나실", "barbecue": "바베큐장", "beverage": "식음료장 여부",
    "campfire": "캠프파이어 가능", "goodstay": "굿스테이 여부", "publicpc": "공용 PC실", "roomtype": "객실유형",
    "foodplace": "식음료장", "roomcount": "객실수", "chkcooking": "객실 내 취사 가능", "publicbath": "공용 샤워실/욕실",
    "checkintime": "체크인 시간", "subfacility": "부대시설", "checkouttime": "체크아웃 시간", "scalelodging": "규모(숙박)",
    "parkinglodging": "주차시설(숙박)", "reservationurl": "예약 URL", "refundregulation": "환불규정",
    "accomcountlodging": "수용인원(숙박)", "infocenterlodging": "문의 및 안내(숙박)", "reservationlodging": "예약안내(숙박)",

    // contentTypeId: 38 (쇼핑)
    "fairday": "장서는 날", "opentime": "영업시간", "restroom": "화장실 유무", "saleitem": "판매품목",
    "shopguide": "매장안내", "saleitemcost": "판매품목별 가격", "culturecenter": "문화센터 유무",
    "scaleshopping": "규모(쇼핑)", "parkingshopping": "주차시설(쇼핑)", "opendateshopping": "개장일(쇼핑)",
    "restdateshopping": "쉬는날(쇼핑)", "infocentershopping": "문의 및 안내(쇼핑)",
    "chkcreditcardshopping": "신용카드(쇼핑)", "chkpetshopping": "반려동물 동반(쇼핑)",

    // contentTypeId: 39 (음식점)
    "seat": "좌석정보", "lcnsno": "인허가번호", "packing": "포장 가능", "smoking": "흡연 가능여부",
    "firstmenu": "대표메뉴", "scalefood": "규모(음식점)", "treatmenu": "취급메뉴",
    "parkingfood": "주차시설(음식점)", "kidsfacility": "어린이 놀이방 유무", "opendatefood": "개업일(음식점)",
    "opentimefood": "영업시간(음식점)", "restdatefood": "쉬는날(음식점)", "infocenterfood": "문의 및 안내(음식점)",
    "reservationfood": "예약안내(음식점)", "discountinfofood": "할인정보(음식점)", "chkcreditcardfood": "신용카드(음식점)",

    // contentTypeId: 15 (축제/공연/행사)
    "program": "행사 프로그램", "agelimit": "관람 가능연령", "playtime": "공연시간",
    "sponsor1": "주최기관", "sponsor2": "주관기관", "subevent": "부대행사",
    "placeinfo": "행사장 위치안내", "eventplace": "행사장소", "sponsor1tel": "주최기관 연락처",
    "sponsor2tel": "주관기관 연락처", "bookingplace": "예매처", "eventenddate": "행사 종료일",
    "eventhomepage": "행사 홈페이지", "festivalgrade": "축제등급", "eventstartdate": "행사 시작일",
    "usetimefestival": "이용요금(축제)", "spendtimefestival": "관람 소요시간(축제)", "discountinfofestival": "할인정보(축제)"
};

/**
 * introKeyToLabelMap에 정의되지 않은 API 키(key)를 사람이 읽기 쉬운 형태로 변환하려고 시도하는 함수입니다.
 * 완벽하지 않으며, 최선의 추측을 기반으로 변환합니다.
 * @param {string} key 변환할 API 키 문자열
 * @returns {string} 변환된 레이블 문자열
 */
function formatIntroKeyFallback(key) {
    if (!key) return "정보"; // 키가 없으면 기본값 반환

    let formattedKey = key;

    // 내부 식별자로 예상되는 키들에 대한 간단한 변환
    if (formattedKey.toLowerCase() === 'contentid') return '콘텐츠 ID';
    if (formattedKey.toLowerCase() === 'contenttypeid') return '콘텐츠 타입 ID';

    // 일반적인 접두어/접미어 기반 변환 규칙 (예시)
    if (formattedKey.startsWith('chk')) {
        formattedKey = formattedKey.substring(3) + ' 가능여부';
    } else if (formattedKey.startsWith('infocenter')) {
        formattedKey = formattedKey.substring(10) + ' 문의';
    } // ... (기존 로직 유지 및 필요시 확장) ...

    // CamelCase 또는 snake_case를 공백으로 분리하고 첫 글자 대문자화
    formattedKey = formattedKey
        .replace(/([A-Z])/g, ' $1') // 대문자 앞에 공백 추가 (CamelCase 처리)
        .replace(/_/g, ' ')        // 언더스코어를 공백으로 변경 (snake_case 처리)
        .trim();                   // 앞뒤 공백 제거
    // 각 단어의 첫 글자를 대문자로 만들고 싶다면 추가 로직 필요
    formattedKey = formattedKey.charAt(0).toUpperCase() + formattedKey.slice(1).toLowerCase(); // 첫 글자만 대문자, 나머지는 소문자로 (선택적)

    return formattedKey;
}

/**
 * 메인 이미지 및 썸네일 이미지들을 로드하여 화면에 표시합니다.
 * @param {string} firstImage - 대표 이미지 URL
 * @param {string} firstImage2 - 보조 대표 이미지 URL
 * @param {Array} additionalImages - 추가 이미지 객체({smallImageUrl?, originImgUrl, imgName}) 목록
 */
function loadThumbnails(firstImage, firstImage2, additionalImages) {
    const mainImageDisplay = document.getElementById('mainContentImage');       // 메인 이미지가 표시될 <img> 태그
    const thumbnailContainer = document.getElementById('thumbnailContainer'); // 썸네일들이 표시될 <div> 컨테이너
    if (!mainImageDisplay || !thumbnailContainer) return; // 필수 요소가 없으면 함수 종료

    thumbnailContainer.innerHTML = ''; // 기존 썸네일 초기화
    const displayedThumbnails = new Set(); // 중복된 썸네일 표시 방지를 위한 Set
    let hasThumbnails = false;             // 실제 표시된 썸네일이 있는지 여부 플래그

    /**
     * 썸네일 이미지 DOM 요소를 생성하고 컨테이너에 추가하는 내부 함수입니다.
     * @param {string} src - 썸네일 이미지 URL
     * @param {string} alt - 이미지 대체 텍스트
     * @param {string} mainSrcToSet - 클릭 시 메인 이미지에 설정될 URL
     * @param {boolean} isActive - 현재 활성화된(선택된) 썸네일인지 여부
     */
    const addThumbnail = (src, alt, mainSrcToSet, isActive = false) => {
        // 유효한 src이고, 대체 이미지가 아니며, 아직 표시되지 않은 썸네일인 경우에만 추가
        if (src && !src.endsWith('/assets/noimg.png') && !displayedThumbnails.has(src)) {
            const thumb = createThumbnailElement(src, alt, mainSrcToSet || src);
            if (isActive) thumb.classList.add('active'); // 활성 썸네일이면 'active' 클래스 추가
            thumbnailContainer.appendChild(thumb);
            displayedThumbnails.add(src); // 표시된 썸네일 Set에 추가
            hasThumbnails = true;
        }
    };

    // 1. 대표 이미지(firstImage)를 첫 번째 활성 썸네일로 추가 (유효한 경우)
    if (firstImage && !firstImage.endsWith('/assets/noimg.png')) {
        addThumbnail(firstImage, (contentDetail.title || "대표") + " 이미지", firstImage, true);
    }


    // 2. 추가 이미지 목록(additionalImagesData) 처리
    if (additionalImages && additionalImages.length > 0) {
        additionalImages.forEach((image, index) => {
            const imgSrc = image.smallImageUrl || image.originImgUrl; // 썸네일 URL 우선, 없으면 원본 URL 사용
            const mainImgSrc = image.originImgUrl || image.smallImageUrl; // 메인에 표시될 이미지는 원본 우선
            // firstImage, firstImage2와 중복되지 않고 유효한 경우에만 추가
            if (imgSrc && imgSrc !== firstImage && imgSrc !== firstImage2) {
                addThumbnail(imgSrc, image.imgName || `${contentDetail.title || "추가"} 이미지 ${index + 2}`, mainImgSrc);
            }
        });
    }

    // 썸네일이 하나도 없는 경우 메시지 처리
    if (!hasThumbnails) {
        // 메인 이미지 자체가 대체 이미지이거나, 어떤 이미지 정보도 없는 경우
        if (mainImageDisplay.src.includes('/assets/noimg.png')) {
            thumbnailContainer.innerHTML = '<p class="text-muted small my-auto">대표 이미지가 없습니다.</p>';
        } else { // 메인 이미지는 있지만 추가 썸네일이 없는 경우 (이 경우는 displayedThumbnails.size === 1일 때와 유사)
            thumbnailContainer.innerHTML = '<p class="text-muted small my-auto">추가 이미지가 없습니다.</p>';
        }
    } else if (displayedThumbnails.size === 1) {
        // 썸네일이 정확히 하나만 있는 경우 (즉, firstImage만 있고 나머지는 없는 경우)
        // 썸네일 스트립에 "추가 이미지가 없습니다" 메시지를 표시하거나, 스트립 자체를 숨길 수 있습니다.
        // 현재는 메시지 표시로 유지
        thumbnailContainer.innerHTML = '<p class="text-muted small my-auto">추가 이미지가 없습니다.</p>';
    }
}

/**
 * 개별 썸네일 이미지 DOM 요소를 생성하고 이벤트 리스너를 설정합니다.
 * @param {string} thumbSrc - 썸네일 이미지의 소스 URL
 * @param {string} altText - 이미지의 대체 텍스트
 * @param {string} mainSrc - 이 썸네일을 클릭했을 때 메인 이미지 영역에 표시될 이미지의 소스 URL
 * @returns {HTMLImageElement} 생성된 썸네일 이미지 요소
 */
function createThumbnailElement(thumbSrc, altText, mainSrc) {
    const mainImageDisplay = document.getElementById('mainContentImage');
    const thumb = document.createElement('img');
    thumb.src = thumbSrc;
    thumb.alt = altText || '썸네일 이미지';
    thumb.className = 'img-thumbnail'; // Bootstrap 썸네일 스타일 적용 (선택적)
    // 이미지 로드 실패 시 해당 썸네일 숨김 (또는 대체 이미지 표시)
    thumb.onerror = function () {
        this.style.display = 'none'; // 또는 this.src = '/assets/noimg_thumb.png';
    };
    // 썸네일 클릭 이벤트: 메인 이미지 변경 및 활성 썸네일 표시 업데이트
    thumb.addEventListener('click', () => {
        if (mainImageDisplay) mainImageDisplay.src = mainSrc; // 메인 이미지 소스 변경
        // 기존 활성 썸네일의 'active' 클래스 제거
        document.querySelectorAll('#thumbnailContainer img.active').forEach(active => active.classList.remove('active'));
        thumb.classList.add('active'); // 현재 클릭된 썸네일에 'active' 클래스 추가
    });
    return thumb;
}


/**
 * "이용 정보" 섹션에 콘텐츠 타입별 소개 정보(introDetailsMap)를 렌더링합니다.
 * @param {Object} introData - 키-값 쌍으로 구성된 소개 정보 객체
 */
function renderIntroDetails(introData) {
    const container = document.getElementById('introDetailsContainer');
    if (!container) return; // 컨테이너 요소가 없으면 함수 종료

    // introData가 비어있거나 null이면 "정보 없음" 메시지 표시
    if (!introData || Object.keys(introData).length === 0) {
        container.innerHTML = '<p class="text-muted col-12">세부 이용 정보가 없습니다.</p>';
        document.getElementById('introductionSection').style.display = 'none'; // 섹션 전체를 숨김
        return;
    }

    let htmlContent = '';
    let itemCount = 0; // 실제 표시된 항목 수 카운트

    // introData 객체의 각 키-값 쌍에 대해 반복 처리
    for (const [key, value] of Object.entries(introData)) {
        const lowerKey = key.toLowerCase(); // 키를 소문자로 변환하여 일관성 있는 처리

        // 화면에 표시하지 않을 내부 식별자 또는 불필요한 키들을 여기서 건너뜁니다.
        if (['contentid', 'contenttypeid', 'fldgubun', 'serialnum', 'cpyrhtdivcd'].includes(lowerKey)) {
            continue;
        }

        const valStr = String(value).trim(); // 값의 타입 변환 및 앞뒤 공백 제거

        // 값이 유효한지(null, 빈 문자열, "none", "없음" 등이 아닌지) 검사합니다.
        // "0"이 유의미한 값(예: "없음" 또는 "0개")으로 사용되는 특정 키가 있다면, 해당 키는 이 조건에서 제외하거나 특별 처리합니다.
        // 예시: kidsfacility (어린이 놀이방)의 경우 "0"은 "없음"을 의미하므로 표시해야 함.
        let shouldDisplay = true;
        if (!value || valStr === '' || valStr.toLowerCase() === 'none' || valStr.toLowerCase() === '없음') {
            shouldDisplay = false;
        }
        if (valStr === '0' && !['kidsfacility' /* "0"이 의미 있는 값을 가지는 다른 키들... */].includes(lowerKey)) {
            shouldDisplay = false;
        }


        if (shouldDisplay) {
            // introKeyToLabelMap에서 한글 레이블을 찾고, 없으면 fallback 함수로 변환 시도
            const label = introKeyToLabelMap[lowerKey] || formatIntroKeyFallback(key);
            let finalDisplayValue = valStr;

            // 특정 키에 대한 값 특별 처리 로직 (예: URL 링크 만들기, '0'/'1'을 '예'/'아니오'로 변경 등)
            if (lowerKey === 'reservationurl' || lowerKey === 'eventhomepage' || lowerKey === 'reservation') { // 예약 URL 처리
                let url = '';
                let linkText = '바로가기';
                const tempDiv = document.createElement('div'); // DOM 파싱을 위해 임시 div 사용
                tempDiv.innerHTML = valStr; // 원본 HTML을 임시 div에 삽입
                const anchorTag = tempDiv.querySelector('a');

                if (anchorTag && anchorTag.href) {
                    url = anchorTag.href;
                    linkText = (anchorTag.textContent || url).trim();
                    linkText = linkText.length > 30 ? linkText.substring(0, 27) + '...' : linkText;
                } else if (!anchorTag && (valStr.toLowerCase().startsWith('http') || valStr.toLowerCase().startsWith('www'))) {
                    url = valStr; // a 태그는 없지만, 문자열 자체가 URL인 경우
                    linkText = url.length > 30 ? url.substring(0, 27) + '...' : url;
                }

                if (url) {
                    if (!/^https?:\/\//i.test(url)) {
                        url = 'http://' + url;
                    }
                    finalDisplayValue = `<a href="${url}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-primary intro-link-button">${linkText} <i class="bi bi-box-arrow-up-right"></i></a>`;
                } else if (valStr && valStr !== '' && valStr.toLowerCase() !== 'none' && valStr.toLowerCase() !== '없음') {
                    finalDisplayValue = valStr; // URL이 아니면 원본 HTML(이미 <br> 포함)을 그대로 사용
                } else {
                    finalDisplayValue = '온라인 예약 정보 없음';
                }
            } else if (lowerKey === 'kidsfacility') { // 어린이 놀이방 유무 (0: 없음, 1: 있음)
                finalDisplayValue = (finalDisplayValue === '0' ? '없음' : (finalDisplayValue === '1' ? '있음' : finalDisplayValue));
            } else if (['hanok', 'chkcooking', 'sauna', 'beauty', 'pickup', 'sports', 'benikia', 'bicycle', 'fitness', 'karaoke', 'seminar', 'barbecue', 'beverage', 'campfire', 'goodstay', 'publicpc', 'publicbath', 'packing', 'smoking', 'restroom', 'culturecenter' /* 그 외 Y/N, 0/1, true/false 성격의 키들 */].includes(lowerKey)) {
                // boolean 성격의 값을 '예'/'아니오' 또는 '있음'/'없음' 등으로 변환
                const trueValues = ['1', 'y', 'yes', 'true', '가능', '있음'];
                const falseValues = ['0', 'n', 'no', 'false', '불가능', '없음'];
                if (trueValues.includes(finalDisplayValue.toLowerCase())) finalDisplayValue = '예'; // 또는 '있음'
                else if (falseValues.includes(finalDisplayValue.toLowerCase())) finalDisplayValue = '아니오'; // 또는 '없음'
            }
            // (추가) 다른 특정 키들에 대한 값 포맷팅 로직이 필요하면 여기에 추가합니다.

            // 각 항목을 Bootstrap 그리드 컬럼으로 구성하여 추가
            htmlContent += `
                <div class="col-md-6 col-lg-4 intro-item">
                    <strong>${label}</strong>
                    <div class="value text-break">${finalDisplayValue}</div>
                </div>`;
            itemCount++; // 유효한 항목 수 증가
        }
    }

    // 유효한 항목이 하나라도 있으면 내용을 채우고, 없으면 "정보 없음" 메시지 표시 및 섹션 숨김
    if (itemCount > 0) {
        container.innerHTML = htmlContent;
        document.getElementById('introductionSection').style.display = ''; // 섹션 보이기
    } else {
        container.innerHTML = '<p class="text-muted col-12">세부 이용 정보가 없습니다.</p>';
        document.getElementById('introductionSection').style.display = 'none'; // 섹션 숨기기
    }
}


/**
 * 페이지 하단의 정보 탭들(지도, 상세정보, 반려동물, 객실 등)을 초기화하고 첫 번째 유효한 탭의 콘텐츠를 로드합니다.
 * 각 탭은 조건(condition)에 따라 표시 여부가 결정됩니다.
 * @param {number} currentContentTypeId - 현재 콘텐츠의 타입 ID
 * @param {number} currentMapX - 현재 콘텐츠의 X 좌표 (경도)
 * @param {number} currentMapY - 현재 콘텐츠의 Y 좌표 (위도)
 * @param {Array} currentInfosData - 상세 정보(텍스트) 목록
 * @param {Object} currentPetInfoData - 반려동물 동반 정보 객체
 * @param {Array} currentRoomData - 객실 정보 목록
 * @param {string} currentPlaceTitle - 장소명 (지도용)
 * @param {string} currentPlaceAddress - 주소 (지도용)
 */
function initializeTabsAndFirstContent(currentContentTypeId, currentMapX, currentMapY, currentInfosData, currentPetInfoData, currentRoomData, currentPlaceTitle, currentPlaceAddress) {
    const tabListContainer = document.getElementById('contentTab');    // 탭 버튼들이 들어갈 <ul> 요소
    const tabContentContainer = document.getElementById('contentTabContent'); // 탭 내용이 들어갈 <div> 요소
    if (!tabListContainer || !tabContentContainer) return; // 필수 컨테이너 없으면 종료

    tabListContainer.innerHTML = '';    // 기존 탭 버튼들 초기화
    tabContentContainer.innerHTML = ''; // 기존 탭 내용 초기화

    // 각 탭의 설정 정보를 담은 배열
    const tabConfigs = [
        { // 지도 탭
            id: 'map-view', // 탭 ID
            title: '지도',    // 탭 제목
            condition: (currentMapX != null && currentMapY != null), // 표시 조건: 좌표값이 모두 있어야 함
            renderer: initKakaoMapOnTab, // 탭 내용 렌더링 함수
            data: { mapX: currentMapX, mapY: currentMapY, title: currentPlaceTitle, address: currentPlaceAddress } // 렌더러에 전달할 데이터
        },
        { // 상세정보 탭 (숙박(32)이 아닐 때 infosData가 있으면 표시)
            id: 'detailed-info',
            title: '상세정보',
            condition: (currentContentTypeId !== 32 && currentInfosData && currentInfosData.length > 0),
            renderer: renderDetailedInfo,
            data: currentInfosData
        },
        { // 반려동물 정보 탭
            id: 'pet-info',
            title: '반려동물 정보',
            condition: (currentPetInfoData && Object.keys(currentPetInfoData).length > 1 && currentPetInfoData.acmpyTypeCd), // 유의미한 데이터가 있을 때
            renderer: renderPetInfo,
            data: currentPetInfoData
        },
        // 숙박(contentTypeId: 32)인 경우에만 객실 정보 탭 추가
        ...(currentContentTypeId === 32 ? [{
            id: 'room-info',
            title: '객실정보',
            condition: (currentRoomData && currentRoomData.length > 0),
            renderer: renderRoomInfo,
            data: currentRoomData
        }] : [])
    ];

    let firstActiveTabSet = false; // 첫 번째 활성 탭이 설정되었는지 여부

    // 설정된 탭들을 순회하며 조건에 맞으면 탭 구조를 생성
    for (const config of tabConfigs) {
        if (config.condition) { // 현재 탭을 표시할 조건이 충족되면
            const isActive = !firstActiveTabSet; // 아직 활성 탭이 없으면 이 탭을 활성 상태로
            createTabStructure(config.id, config.title, isActive, config.renderer, config.data);

            if (isActive) { // 이 탭이 첫 번째 활성 탭이라면
                firstActiveTabSet = true;
                // 활성 탭의 내용을 즉시 로드 (Lazy loading 대신 Eager loading)
                const pane = document.getElementById(`${config.id}-pane`);
                if (pane && config.renderer) {
                    config.renderer(pane, config.data); // 렌더링 함수 호출
                    pane.dataset.loaded = 'true';    // 이미 로드되었음을 표시 (Lazy loading 방지)
                }
            }
        }
    }

    // 생성된 탭이 하나도 없으면 "추가 정보 없음" 메시지 표시
    if (tabListContainer.children.length === 0) {
        tabListContainer.innerHTML = '<li class="nav-item flex-grow-1 text-center p-3 text-muted border rounded-top">표시할 추가 정보 탭이 없습니다.</li>';
        tabContentContainer.style.display = 'none'; // 탭 내용 영역 숨김
    } else {
        tabContentContainer.style.display = 'block'; // 탭이 있으면 내용 영역 보이기
    }
}

/**
 * Bootstrap 탭 구조(버튼과 내용 패널)를 동적으로 생성합니다.
 * @param {string} id - 탭 및 패널의 고유 ID (접두사로 사용)
 * @param {string} title - 탭 버튼에 표시될 텍스트
 * @param {boolean} isActive - 이 탭이 초기에 활성화될지 여부
 * @param {Function} contentRenderer - 탭이 활성화될 때 내용을 렌더링할 콜백 함수
 * @param {Object} dataForRenderer - contentRenderer 함수에 전달될 데이터
 */
function createTabStructure(id, title, isActive, contentRenderer, dataForRenderer) {
    const tabListContainer = document.getElementById('contentTab');
    const tabContentContainer = document.getElementById('contentTabContent');

    // 탭 버튼(<li><a> 또는 <li><button>) 생성
    const li = document.createElement('li');
    li.className = 'nav-item';
    li.setAttribute('role', 'presentation');

    const button = document.createElement('button');
    button.className = `nav-link ${isActive ? 'active' : ''}`; // 활성 상태에 따라 'active' 클래스 추가
    button.id = `${id}-tab`; // 탭 버튼 ID
    button.dataset.bsToggle = 'tab'; // Bootstrap 탭 기능 활성화
    button.dataset.bsTarget = `#${id}-pane`; // 이 버튼과 연결될 탭 패널의 ID
    button.type = 'button';
    button.role = 'tab';
    button.setAttribute('aria-controls', `${id}-pane`);
    button.setAttribute('aria-selected', isActive ? 'true' : 'false');
    button.textContent = title;
    li.appendChild(button);
    tabListContainer.appendChild(li);

    // 탭 내용 패널(<div>) 생성
    const pane = document.createElement('div');
    pane.className = `tab-pane fade ${isActive ? 'show active' : ''}`; // 활성 상태에 따라 'show active' 클래스 추가
    pane.id = `${id}-pane`; // 탭 패널 ID
    pane.setAttribute('role', 'tabpanel');
    pane.setAttribute('aria-labelledby', `${id}-tab`);
    // 초기에는 로딩 스피너 표시
    pane.innerHTML = '<div class="d-flex justify-content-center align-items-center my-3" style="min-height: 150px;"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></div>';
    tabContentContainer.appendChild(pane);

    // 탭이 보여질 때(show.bs.tab 이벤트) 내용을 로드하는 이벤트 리스너 (Lazy loading)
    // 단, initializeTabsAndFirstContent에서 첫 활성 탭은 이미 Eager loading 처리함.
    if (contentRenderer) {
        button.addEventListener('show.bs.tab', () => {
            // 아직 내용이 로드되지 않은 경우에만 렌더링 함수 호출
            if (!pane.dataset.loaded) {
                contentRenderer(pane, dataForRenderer);
                pane.dataset.loaded = 'true'; // 로드되었음을 표시
            }
        });
    }
}

/**
 * "상세정보" 탭의 내용을 렌더링합니다. (infosData 사용)
 * @param {HTMLElement} pane - 내용이 표시될 탭 패널 요소
 * @param {Array} data - 상세 정보(텍스트) 객체 목록 (각 객체는 infoName, infoText 필드 가짐)
 */
function renderDetailedInfo(pane, data) {
    if (data && data.length > 0) {
        let html = '<dl class="row g-3 pt-3">'; // dl: description list
        data.forEach(item => {
            const infoName = item.infoName || '정보'; // 제목이 없으면 '정보'
            const infoText = (item.infoText || '-').replace(/\n/g, '<br />'); // 내용이 없으면 '-', 줄바꿈 처리
            html += `
                <dt class="col-sm-3 text-truncate" title="${infoName}">${infoName}</dt> <dd class="col-sm-9"><p class="mb-2 text-break">${infoText}</p></dd>     `;
        });
        html += '</dl>';
        pane.innerHTML = html;
    } else {
        pane.innerHTML = '<p class="text-muted p-3">추가 상세 정보가 없습니다.</p>';
    }
}

/**
 * "반려동물 정보" 탭의 내용을 렌더링합니다. (petTourInfoData 사용)
 * @param {HTMLElement} pane - 내용이 표시될 탭 패널 요소
 * @param {Object} data - 반려동물 동반 정보 객체
 */
function renderPetInfo(pane, data) {
    // 데이터가 유효하고, acmpyTypeCd (동반 유형) 정보가 있는 경우에만 표시 시도
    if (data && Object.keys(data).length > 1 && data.acmpyTypeCd) {
        let html = '<div class="pet-info-section pt-3"><dl class="row g-3">';
        // 표시할 반려동물 정보 키와 한글 레이블 매핑
        const petInfoMap = {
            'acmpyTypeCd': '동반 유형', 'acmpyPsblCpam': '동반 가능 동물', 'acmpyNeedMtr': '필수 준비물',
            'relaPosesFclty': '반려동물 시설', 'relaFrnshPrdlst': '비치 용품', 'relaRntlPrdlst': '대여 가능 용품',
            'relaPurcPrdlst': '구매 가능 용품', 'relaAcdntRiskMtr': '안전 주의사항', 'etcAcmpyInfo': '기타 정보'
        };
        let itemCount = 0; // 실제 표시된 항목 수
        for (const [key, label] of Object.entries(petInfoMap)) {
            // 해당 키의 값이 유효한 경우(null, 빈 문자열, "none", "없음" 등이 아님)에만 표시
            const valueStr = data[key] ? String(data[key]).trim() : '';
            if (valueStr && valueStr.toLowerCase() !== 'none' && valueStr.toLowerCase() !== '없음') {
                html += `<dt class="col-sm-4 col-md-3">${label}</dt><dd class="col-sm-8 col-md-9 text-break">${valueStr.replace(/\n/g, '<br />')}</dd>`;
                itemCount++;
            }
        }
        html += '</dl></div>';
        pane.innerHTML = itemCount > 0 ? html : '<p class="text-muted p-3">반려동물 관련 상세 정보가 없습니다.</p>';
    } else {
        pane.innerHTML = '<p class="text-muted p-3">반려동물 동반 가능 정보가 없거나, 지원하지 않는 장소입니다.</p>';
    }
}

/**
 * "객실정보" 탭의 내용을 렌더링합니다. (roomData 사용)
 * @param {HTMLElement} pane - 내용이 표시될 탭 패널 요소
 * @param {Array} data - 객실 정보 객체 목록
 */
function renderRoomInfo(pane, data) {
    if (data && data.length > 0) {
        let html = '<div class="room-list container-fluid px-0 pt-3">';
        data.forEach(room => {
            // 요금 정보 포맷팅 (숫자로 변환 후 지역화된 문자열로)
            const formatPrice = (priceStr) => {
                const priceNum = Number(priceStr);
                return !isNaN(priceNum) && priceNum !== 0 ? priceNum.toLocaleString() + '원' : '';
            };
            const offSeasonFee = formatPrice(room.roomOffSeasonMinFee1);
            const peakSeasonFee = formatPrice(room.roomPeakSeasonMinFee1);

            html += `
                <div class="card room-card mb-3">
                    <div class="row g-0">
                        <div class="col-md-4 col-lg-3">
                            <img src="${room.roomImg1 || '/assets/noimg.png'}" class="img-fluid rounded-start room-image" 
                                 alt="${room.roomTitle || '객실 이미지'}" 
                                 onerror="this.onerror=null; this.src='/assets/noimg.png';">
                        </div>
                        <div class="col-md-8 col-lg-9">
                            <div class="card-body">
                                <h5 class="card-title mb-2">${room.roomTitle || '객실명 없음'}</h5>
                                <p class="card-text small mb-2 text-break">${(room.roomIntro || '객실 소개가 없습니다.').replace(/\n/g, '<br />')}</p>
                                <ul class="list-unstyled small mb-2 room-specs">
                                    ${room.roomBaseCount ? `<li><i class="bi bi-people text-muted me-1"></i>기준 ${room.roomBaseCount}명 (최대 ${room.roomMaxCount || room.roomBaseCount}명)</li>` : ''}
                                    ${room.roomSize1 ? `<li><i class="bi bi-aspect-ratio text-muted me-1"></i>크기: ${room.roomSize1} ${room.roomSize2 ? '(' + room.roomSize2 + ')' : ''}</li>` : ''}
                                    ${offSeasonFee ? `<li><i class="bi bi-tags text-muted me-1"></i>비수기(주중): ${offSeasonFee}</li>` : ''}
                                    ${peakSeasonFee ? `<li><i class="bi bi-tag-fill text-muted me-1"></i>성수기(주중): ${peakSeasonFee}</li>` : ''}
                                </ul>
                                <div class="room-facilities mt-2">
                                    ${room.roomCook === 'Y' ? '<span class="badge bg-success-subtle text-success-emphasis fw-normal me-1 mb-1"><i class="bi bi-fire me-1"></i>취사가능</span>' : ''}
                                    ${room.roomBathFacility === 'Y' ? '<span class="badge bg-info-subtle text-info-emphasis fw-normal me-1 mb-1"><i class="bi bi-droplet-fill me-1"></i>욕실</span>' : ''}
                                    ${room.roomInternet === 'Y' ? '<span class="badge bg-secondary-subtle text-secondary-emphasis fw-normal me-1 mb-1"><i class="bi bi-wifi me-1"></i>인터넷</span>' : ''}
                                    </div>
                            </div>
                        </div>
                    </div>
                </div>`;
        });
        html += '</div>';
        pane.innerHTML = html;
    } else {
        pane.innerHTML = '<p class="text-muted p-3">등록된 객실 정보가 없습니다.</p>';
    }
}

let kakaoMapInstance = null; // 카카오 지도 인스턴스를 저장할 변수 (탭 전환 시 재사용 위함)

/**
 * "지도" 탭에 카카오 지도를 초기화하고 표시합니다.
 * @param {HTMLElement} pane - 지도가 표시될 탭 패널 요소
 * @param {Object} mapData - 지도 표시에 필요한 데이터 {mapX, mapY, title, address}
 */
function initKakaoMapOnTab(pane, mapData) {
    // 지도 컨테이너 div 생성 및 pane에 추가
    pane.innerHTML = `<div id="kakaoDetailMap" style="width:100%;height:400px; border-radius: var(--bs-border-radius-sm);"></div>
                      <p class="form-text text-center mt-2">지도는 마우스 휠로 확대/축소, 드래그로 이동할 수 있습니다.</p>`;

    const mapContainer = pane.querySelector('#kakaoDetailMap'); // 실제 지도가 그려질 div

    // 지도 표시에 필요한 좌표 정보가 없는 경우
    if (!mapData || mapData.mapX == null || mapData.mapY == null) { // null 또는 undefined 체크
        mapContainer.innerHTML = "<p class='text-warning p-3 text-center'>지도를 표시할 위치 정보가 없습니다.</p>";
        console.warn("카카오 지도: 좌표 정보 누락", mapData);
        return;
    }
    // 카카오 지도 API 스크립트가 제대로 로드되지 않은 경우
    if (!window.kakao || !window.kakao.maps) {
        mapContainer.innerHTML = "<p class='text-danger p-3 text-center'>카카오 지도 API를 불러오지 못했습니다. 페이지를 새로고침하거나 인터넷 연결을 확인해주세요.</p>";
        console.error("카카오 지도 API 로드 실패");
        return;
    }

    try {
        const position = new kakao.maps.LatLng(mapData.mapY, mapData.mapX);
        const mapOptions = {
            center: position, // 지도의 중심좌표
            level: 4          // 지도의 확대 레벨
        };

        // 지도가 이미 생성되었다면 중심 위치만 이동하고, 아니면 새로 생성
        if (kakaoMapInstance && kakaoMapInstance.getMapTypeId()) { // getMapTypeId() 등으로 유효한 인스턴스인지 확인
            // 중요: 탭 전환 등으로 mapContainer가 DOM에서 분리되었다가 다시 붙는 경우,
            // 기존 인스턴스에 setContainer를 호출하거나, relayout만으로는 부족할 수 있습니다.
            // 가장 안정적인 방법은 탭이 보일 때마다 지도를 새로 그리거나,
            // 지도를 담을 div를 탭 패널 외부에 두고 탭 패널에는 해당 div의 참조만 남기는 것입니다.
            // 여기서는 간단히 중심 이동 및 relayout으로 처리합니다.
            // 만약 지도가 제대로 보이지 않는다면, 지도를 매번 새로 생성하는 것을 고려해야 합니다.
            // kakaoMapInstance = new kakao.maps.Map(mapContainer, mapOptions); // 매번 새로 생성하는 경우
            kakaoMapInstance.setCenter(position);
            kakaoMapInstance.setLevel(mapOptions.level);
        } else {
            kakaoMapInstance = new kakao.maps.Map(mapContainer, mapOptions);
        }

        // 일반 지도와 스카이뷰로 지도 타입을 전환할 수 있는 지도타입 컨트롤을 생성합니다
        const mapTypeControl = new kakao.maps.MapTypeControl();
        kakaoMapInstance.addControl(mapTypeControl, kakao.maps.ControlPosition.TOPRIGHT);

        // 지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
        const zoomControl = new kakao.maps.ZoomControl();
        kakaoMapInstance.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);


        // 지도 드래그 및 확대/축소 기능 활성화
        kakaoMapInstance.setDraggable(true);
        kakaoMapInstance.setZoomable(true);

        // 마커 생성 및 지도에 표시
        const marker = new kakao.maps.Marker({ position: position });
        marker.setMap(kakaoMapInstance);

        // 인포윈도우 내용 구성
        const iwContent = `
    <div class="kakao-infowindow p-2 shadow-sm rounded" style="min-width: 300px;">
        <strong class="d-block mb-1 text-center" style="font-size: 1rem;">${mapData.title || '선택 위치'}</strong>
        <span class="text-muted d-block text-center" style="font-size: 0.85rem;">${mapData.address || '주소 정보 없음'}</span>
        <div class="mt-2 mb-2 d-flex justify-content-center">
            <a href="https://map.kakao.com/link/to/${mapData.title || '선택 위치'},${mapData.mapY},${mapData.mapX}" target="_blank" class="btn btn-sm btn-primary me-3">길찾기</a>
            <a href="https://map.kakao.com/link/map/${mapData.title || '선택 위치'},${mapData.mapY},${mapData.mapX}" target="_blank" class="btn btn-sm btn-secondary">큰 지도</a>
        </div>
    </div>`;
        const infowindow = new kakao.maps.InfoWindow({ content: iwContent, removable: true });
        infowindow.open(kakaoMapInstance, marker); // 마커 위에 인포윈도우 표시

        // 탭이 표시된 후 지도가 올바르게 그려지도록 잠시 후 relayout 호출 (특히 처음 로드될 때 중요)
        setTimeout(() => {
            if (kakaoMapInstance && mapContainer.offsetParent !== null) { // mapContainer가 화면에 보이는지 확인
                kakaoMapInstance.relayout();
            }
        }, 50); // 딜레이는 상황에 따라 조절

    } catch (e) {
        console.error("카카오 지도 초기화 중 오류 발생:", e);
        mapContainer.innerHTML = "<p class='text-danger p-3 text-center'>지도 표시에 실패했습니다. 브라우저 콘솔을 확인해주세요.</p>";
    }
}