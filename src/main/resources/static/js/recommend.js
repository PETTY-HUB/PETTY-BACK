let directSearchCurrentPage = 0;
const DIRECT_SEARCH_PAGE_SIZE = 10;
let directSearchTotalPages = 0;
let directSearchSelectedLatlng = null; // 직접 검색용 좌표 저장
let lastManualAiRecommendResponse = null;

let map;
let geocoder;
let marker;
let infowindow;
let currentMapTargetInputId = null;
let selectedLatlng = null; // This will store the LngLat object from map click/search
let kakaoMapsApiIsReady = false;

const TOUR_TYPE_MAP_DIRECT = {
    12: '관광지', 14: '문화시설', 15: '축제', 25: '여행코스', // 25는 예시, 실제 값 확인 필요
    28: '레포츠', 32: '숙박', 38: '쇼핑', 39: '음식점' // 38은 예시, 실제 값 확인 필요
};
let sigunguDataCache = {};

function initializeKakaoDependentFeatures() {
    if (kakaoMapsApiIsReady) return;
    kakaoMapsApiIsReady = true;
    console.log("Kakao Maps API is confirmed ready. Initializing features.");

    geocoder = new kakao.maps.services.Geocoder();
    infowindow = new kakao.maps.InfoWindow({
        zIndex: 1,
        removable: true
    });

    const mapContainer = document.getElementById('map');
    if (mapContainer) {
        const mapOption = {
            center: new kakao.maps.LatLng(37.566826, 126.9786567),
            level: 5,
        };
        map = new kakao.maps.Map(mapContainer, mapOption);
        marker = new kakao.maps.Marker({ position: map.getCenter() });
        marker.setMap(map);

        kakao.maps.event.addListener(map, 'click', function (mouseEvent) {
            selectedLatlng = mouseEvent.latLng; // Store the LngLat object
            marker.setPosition(selectedLatlng);

            searchDetailAddrFromCoords(selectedLatlng, function (result, status) {
                let content = '';
                let simpleAddr = '주소 정보 없음';
                if (status === kakao.maps.services.Status.OK && result.length > 0) {
                    simpleAddr = getSimpleAddressFromKakao(result[0]);
                    const addressName = result[0].address ? result[0].address.address_name : (result[0].road_address ? result[0].road_address.address_name : simpleAddr);
                    content = `<div class="petty-map-infowindow"><div class="info-address">${addressName}</div></div>`;
                } else {
                    content = `<div class="petty-map-infowindow"><div class="info-address">주소를 가져올 수 없습니다.</div></div>`;
                }
                infowindow.setContent(content);
                infowindow.open(map, marker);

                if (currentMapTargetInputId) {
                    const targetInput = document.getElementById(currentMapTargetInputId);
                    if (targetInput) {
                        targetInput.value = simpleAddr;
                    }
                }
            });
        });
    } else {
        console.error("Map container element with ID 'map' not found for initialization.");
        kakaoMapsApiIsReady = false;
    }

    const mapModalElement = document.getElementById('mapModal');
    if (mapModalElement) {
        mapModalElement.addEventListener('shown.bs.modal', function () {
            console.log("Map modal 'shown.bs.modal' event fired. currentMapTargetInputId:", currentMapTargetInputId);
            console.log("Current selectedLatlng (generic for modal):", selectedLatlng ? selectedLatlng.toString() : "null");
            console.log("Current directSearchSelectedLatlng (for manual search):", directSearchSelectedLatlng ? directSearchSelectedLatlng.toString() : "null");

            if (map && typeof map.relayout === 'function') {
                console.log("Map object and relayout function are available. Calling relayout...");
                map.relayout();
                console.log("map.relayout() called.");

                let centerPosition;
                if (currentMapTargetInputId === 'manualLocationInput' && directSearchSelectedLatlng instanceof kakao.maps.LatLng) {
                    centerPosition = directSearchSelectedLatlng;
                    console.log("Using 'directSearchSelectedLatlng' for map center:", centerPosition.toString());
                } else if (selectedLatlng instanceof kakao.maps.LatLng) {
                    centerPosition = selectedLatlng;
                    console.log("Using 'selectedLatlng' for map center:", centerPosition.toString());
                } else {
                    centerPosition = new kakao.maps.LatLng(37.566826, 126.9786567); // 기본값: 서울시청
                    console.log("No valid pre-selected LatLng. Defaulting map center to Seoul City Hall:", centerPosition.toString());
                }

                map.setCenter(centerPosition);
                console.log("map.setCenter() called with:", centerPosition.toString());
                if (marker) marker.setPosition(centerPosition);

                const mapDiv = document.getElementById('map');
                console.log("Map div clientHeight:", mapDiv ? mapDiv.clientHeight : "N/A", "offsetWidth:", mapDiv ? mapDiv.offsetWidth : "N/A");
                if(mapDiv && (mapDiv.clientHeight === 0 || mapDiv.offsetWidth === 0)) {
                    console.warn("지도 div의 크기가 0입니다. CSS를 확인하세요.");
                }

            } else {
                console.error("Map object or relayout function NOT available when modal shown. Map object:", map);
                if(!map) console.error("Hint: 'map' variable is not initialized or not accessible here.");
            }
        });
    }
}

function waitForKakaoMaps(callback) {
    if (window.kakao && window.kakao.maps && window.kakao.maps.services) {
        try {
            new window.kakao.maps.services.Geocoder(); // Geocoder 생성 가능 여부 확인
            new window.kakao.maps.services.Places();   // Places 서비스 사용 가능 여부 확인
            // 모든 서비스가 준비되었다고 판단되면 콜백 실행
            callback();
        } catch (e) {
            // 서비스 중 하나라도 준비되지 않았다면, 잠시 후 재시도
            console.warn("Kakao Maps services (Geocoder or Places) not fully ready, retrying...", e);
            setTimeout(() => waitForKakaoMaps(callback), 200);
        }
    } else {
        // Kakao SDK 자체가 아직 로드되지 않았다면, 잠시 후 재시도
        console.warn("Kakao Maps SDK not available yet, retrying...");
        setTimeout(() => waitForKakaoMaps(callback), 200);
    }
}


// --- 지역 코드 관련 함수 ---
function populateDropdown(selectElement, items, defaultOptionText = "전체") {
    if (!selectElement) return;
    selectElement.innerHTML = `<option value="" selected>${defaultOptionText}</option>`; // 기본 옵션
    if (items && items.length > 0) {
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item.code;
            option.textContent = item.name;
            selectElement.appendChild(option);
        });
        selectElement.disabled = false;
    } else {
        selectElement.disabled = true;
    }
}

function loadAreaCodes() {
    const directSearchAreaCodeSelect = document.getElementById('directSearchAreaCode');
    if (!directSearchAreaCodeSelect) {
        console.error("ID 'directSearchAreaCode' 요소를 찾을 수 없습니다."); // ID 수정: directSearchAreaCodeSelect -> directSearchAreaCode
        return;
    }

    const areas = [
        { code: "1", name: "서울" }, { code: "2", name: "인천" }, { code: "3", name: "대전" },
        { code: "4", name: "대구" }, { code: "5", name: "광주" }, { code: "6", name: "부산" },
        { code: "7", name: "울산" }, { code: "8", name: "세종" }, { code: "31", name: "경기도" },
        { code: "32", name: "강원특별자치도" }, { code: "33", name: "충청북도" }, { code: "34", name: "충청남도" },
        { code: "35", name: "경상북도" }, { code: "36", name: "경상남도" }, { code: "37", name: "전북특별자치도" },
        { code: "38", name: "전라남도" }, { code: "39", name: "제주특별자치도" }
    ];

    populateDropdown(directSearchAreaCodeSelect, areas, "시/도 선택");
    console.log("하드코딩된 시/도 목록 로드 완료.");
}

function loadSigunguCodes(areaCode) {
    const directSearchSigunguCodeSelect = document.getElementById('directSearchSigunguCode');
    if (!directSearchSigunguCodeSelect) {
        console.error("ID 'directSearchSigunguCode' 요소를 찾을 수 없습니다."); // ID 수정: directSearchSigunguCodeSelect -> directSearchSigunguCode
        return;
    }

    populateDropdown(directSearchSigunguCodeSelect, [], "시/군/구 선택");
    directSearchSigunguCodeSelect.disabled = true;

    if (!areaCode) {
        return;
    }

    // 1. 캐시 확인
    if (sigunguDataCache[areaCode]) {
        console.log(`[캐시 사용] areaCode ${areaCode}의 시/군/구 목록을 캐시에서 로드합니다.`);
        populateDropdown(directSearchSigunguCodeSelect, sigunguDataCache[areaCode], "시/군/구 전체");
        // populateDropdown 함수 내에서 selectElement.disabled = false; 처리하므로 여기서 별도 호출 안해도 됨
        return; // 캐시된 데이터 사용 후 함수 종료
    }

    // 2. 캐시 없으면 API 호출
    console.log(`[API 호출] areaCode ${areaCode}의 시/군/구 목록을 API에서 가져옵니다.`);
    fetch(`/api/tour/codes?areaCode=${areaCode}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('시/군/구 정보 로드 실패: ' + response.statusText);
            }
            return response.json();
        })
        .then(sigunguList => {
            sigunguDataCache[areaCode] = sigunguList; // 3. API 응답 성공 시 캐시에 저장
            populateDropdown(directSearchSigunguCodeSelect, sigunguList, "시/군/구 전체");
            console.log(`areaCode ${areaCode}에 대한 시/군/구 목록 로드 및 캐시 완료:`, sigunguList);
        })
        .catch(error => {
            console.error(`areaCode ${areaCode}의 시/군/구 정보를 가져오는 중 오류 발생:`, error);
            populateDropdown(directSearchSigunguCodeSelect, [], "로드 실패");
        });
}

// --- Fetch Direct Search Results using API ---
function fetchDirectSearchResults(append = false) {
    const spinner = document.getElementById('spinnerDirectSearch');
    const resultsControls = document.getElementById('resultControlsDirectSearch');
    const loadMoreContainer = document.getElementById('loadMoreContainerDirectSearch');
    const resultListDiv = document.getElementById('directSearchResultList');

    if(spinner) spinner.style.display = 'block';
    if(resultsControls) resultsControls.style.display = 'none'; // 검색 시작 시 결과 컨트롤 숨김
    if(!append && loadMoreContainer) loadMoreContainer.style.display = 'none';


    let baseUrl = '';
    const params = new URLSearchParams();
    params.append('page', directSearchCurrentPage);
    params.append('size', DIRECT_SEARCH_PAGE_SIZE);

    const keywordVal = document.getElementById('directSearchKeyword').value;
    const selectedContentTypeRadio = document.querySelector('input[name="directSearchContentTypeId"]:checked');
    const contentTypeId = selectedContentTypeRadio ? selectedContentTypeRadio.value : null;

    if (contentTypeId) {
        params.append('contentTypeId', contentTypeId);
    }
    // 키워드는 지역/위치 검색 시 백엔드에서 지원하는 경우에만 추가 (현재는 주석 처리된 부분 참고)
    // 백엔드에서 지역/위치 검색 시 키워드도 함께 필터링하도록 지원한다면 아래 주석 해제
    // if (keywordVal) params.append('keyword', keywordVal);


    if (document.getElementById('useCurrentLocationSwitch').checked) {
        baseUrl = '/api/tour/search/location';
        if (!directSearchSelectedLatlng) {
            // alert('지도에서 현재 위치를 선택해주세요.'); // UX 개선: alert 대신 다른 방식 고려
            if(resultListDiv) resultListDiv.innerHTML = '<p class="text-center text-warning col-12 mt-4">지도에서 현재 위치를 선택해주세요.</p>';
            if (spinner) spinner.style.display = 'none';
            return;
        }
        params.append('mapX', directSearchSelectedLatlng.getLng());
        params.append('mapY', directSearchSelectedLatlng.getLat());
        params.append('radius', document.getElementById('directSearchRadius').value);
        if (keywordVal) params.append('keyword', keywordVal); // 위치 기반 검색 시 키워드 추가 (백엔드 지원 필요)
    } else {
        baseUrl = '/api/tour/search/area';
        const areaCode = document.getElementById('directSearchAreaCode').value;
        const sigunguCode = document.getElementById('directSearchSigunguCode').value;

        if (!areaCode && !keywordVal) { // 지역코드도 없고 키워드도 없으면 검색 불가
            // alert('시/도를 선택하거나 키워드를 입력해주세요.'); // UX 개선: alert 대신 다른 방식 고려
            if(resultListDiv) resultListDiv.innerHTML = '<p class="text-center text-warning col-12 mt-4">시/도를 선택하거나 키워드를 입력해주세요.</p>';
            if (spinner) spinner.style.display = 'none';
            return;
        }
        if (areaCode) params.append('areaCode', areaCode);
        if (sigunguCode) params.append('sigunguCode', sigunguCode);
        if (keywordVal && !areaCode) { // 지역코드가 없을 때만 키워드 단독 검색 허용 (백엔드 설계에 따라 조정)
            params.append('keyword', keywordVal); // 지역 기반 검색 시 키워드 추가 (백엔드 지원 필요)
        } else if (keywordVal && areaCode) { // 지역코드와 키워드가 모두 있을 때 (백엔드 지원 필요)
            params.append('keyword', keywordVal);
        }
    }

    const fullUrl = `${baseUrl}?${params.toString()}`;
    console.log("Direct Search API URL:", fullUrl);

    fetch(fullUrl)
        .then(response => {
            if (spinner) spinner.style.display = 'none';
            if (!response.ok) {
                response.text().then(text => { // 에러 메시지 본문 확인
                    console.error(`HTTP error! status: ${response.status} - ${response.statusText}`, text);
                    throw new Error(`서버 응답 오류 (${response.status}): ${text || response.statusText}`);
                });
            }
            return response.json();
        })
        .then(responseData => {
            if (resultsControls) resultsControls.style.display = 'flex';
            displayDirectSearchResults(responseData.content, append); // *** 여기가 올바른 displayDirectSearchResults 호출 ***

            const resultCountEl = document.getElementById('resultCountDirectSearch');
            if (resultCountEl && responseData.page) {
                resultCountEl.textContent = responseData.page.totalElements; // 변경점
            }
            if (responseData.page) {
                directSearchTotalPages = responseData.page.totalPages; // 변경점
            }
            if (loadMoreContainer && responseData.page) {
                if (responseData.page.number < responseData.page.totalPages - 1) { // 변경점
                    loadMoreContainer.style.display = 'block';
                } else {
                    loadMoreContainer.style.display = 'none';
                }
            }
            // directSearchCurrentPage++; // 더보기 성공 시 증가하도록 아래 displayDirectSearchResults 후로 이동 또는 여기에 유지 (현재 로직은 여기서 증가)
        })
        .catch(error => {
            if (spinner) spinner.style.display = 'none';
            console.error('직접 검색 오류:', error);
            if (resultListDiv) resultListDiv.innerHTML = `<p class="text-center text-danger col-12">검색 결과를 가져오는 중 오류가 발생했습니다: ${error.message}</p>`;
        });
}


function displayDirectSearchResults(items, append = false) {
    const container = document.getElementById('directSearchResultList');
    if (!append) {
        container.innerHTML = '';
    }

    if (!items || items.length === 0) {
        if (!append) { // 새 검색인데 결과가 없을 때만 메시지 표시
            container.innerHTML = '<p class="text-center text-muted col-12 mt-4">검색된 결과가 없습니다.</p>';
        }
        // 결과가 없으면 "더 불러오기" 버튼 숨김 (fetchDirectSearchResults 에서도 처리하지만 여기서도 방어)
        const loadMoreContainer = document.getElementById('loadMoreContainerDirectSearch');
        if (loadMoreContainer) loadMoreContainer.style.display = 'none';
        return;
    }

    items.forEach(item => {
        const colDiv = document.createElement('div');
        // colDiv.className = 'col-12'; // 부트스트랩 그리드 사용 시, 또는 자체 스타일링
        // petty-list-item 자체가 이미 너비를 차지하므로 col-12는 중복일 수 있음 (HTML 구조에 따라 다름)
        // recommend.html 에서는 #directSearchResultList.row > div.col-12 > .petty-list-item 구조를 의도한 것으로 보임
        // 따라서 colDiv에 col-12를 주고, 그 안에 petty-list-item을 넣는 것이 맞을 수 있음.
        // 현재는 petty-list-item이 직접 추가되므로, HTML 구조와 일치시키려면 아래와 같이 수정:
        colDiv.className = 'col-12'; // 이 div가 Bootstrap row의 컬럼 역할

        const detailLink = `/contents/${item.contentId}`;
        const imageUrl = item.firstImage || '/assets/noimg.png';
        const title = item.title || '제목 없음';
        const addr = item.addr1 || '주소 정보 없음';
        const contentType = item.contentTypeId ? (TOUR_TYPE_MAP_DIRECT[item.contentTypeId.toString()] || '기타') : '기타';

        let distanceTag = '';
        if (item.distanceMeters && typeof item.distanceMeters === 'number') {
            distanceTag = `<span class="badge bg-light text-dark border me-1">${(item.distanceMeters / 1000).toFixed(1)}km</span>`;
        }

        // HTML 구조는 recommend.css의 .petty-list-item 스타일과 일치해야 함.
        colDiv.innerHTML = `
                <div class="petty-list-item d-flex mb-3"> 
                    <div class="petty-list-item-img-wrapper">
                        <img src="${imageUrl}" alt="${title}" class="petty-list-item-img">
                    </div>
                    <div class="petty-list-item-content flex-grow-1">
                        <div>
                            <h5 class="petty-list-item-title mb-1"><a href="${detailLink}" target="_blank" class="text-decoration-none">${title}</a></h5>
                            <p class="petty-list-item-addr small text-muted mb-2">${addr}</p>
                        </div>
                        <div class="petty-list-item-tags">
                            <span class="badge bg-info text-dark me-1">${contentType}</span>
                            ${distanceTag}
                        </div>
                    </div>
                    <div class="petty-list-item-actions p-2">
                        <a href="${detailLink}" target="_blank" class="btn btn-sm btn-outline-primary">상세보기</a>
                    </div>
                </div>
            `;
        container.appendChild(colDiv);
    });
    // API 호출 성공 후 페이지 번호 업데이트 (더보기 로직 위해)
    // directSearchCurrentPage = ??? -> fetchDirectSearchResults에서 pageData.number를 기준으로 설정하는 것이 더 안전.
    // 현재는 loadMoreButton 클릭 시와 form submit 시 각각 페이지 번호를 관리하고 있음.
}

document.addEventListener('DOMContentLoaded', function () {
    const aiLocationInput = document.getElementById('aiLocationInput');
    const aiLocationMapButton = document.getElementById('aiLocationMapButton');
    const manualLocationInput = document.getElementById('manualLocationInput'); // 직접 검색 탭의 위치 입력 필드
    const keywordMapInput = document.getElementById('keywordMapInput'); // 모달 내 키워드 입력
    const confirmLocationButton = document.getElementById('confirmLocationButton'); // 모달 내 '선택 완료' 버튼

    console.log("DOM Content Loaded. Waiting for Kakao Maps SDK...");
    waitForKakaoMaps(initializeKakaoDependentFeatures);

    function openMapModal(targetInputId, initialCoords) {
        if (!kakaoMapsApiIsReady) {
            alert("지도 서비스가 준비 중입니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        currentMapTargetInputId = targetInputId;
        selectedLatlng = initialCoords || null;

        if(keywordMapInput) keywordMapInput.value = '';
        if (infowindow) infowindow.close();

        const targetInputElement = document.getElementById(targetInputId);
        const currentLocationValue = targetInputElement ? targetInputElement.value : '';

        if (!selectedLatlng && currentLocationValue && geocoder) {
            console.log(`지도 열기 (${targetInputId}): 입력된 위치 "${currentLocationValue}"로 주소 검색 시도`);
            geocoder.addressSearch(currentLocationValue, function(result, status) {
                if (status === kakao.maps.services.Status.OK && result.length > 0) {
                    selectedLatlng = new kakao.maps.LatLng(result[0].y, result[0].x);
                    console.log(`지도 (${targetInputId}): 주소 검색 성공, 좌표 설정됨`, selectedLatlng);
                } else {
                    console.warn(`지도 (${targetInputId}): 주소 검색 실패 또는 결과 없음`, status);
                    selectedLatlng = new kakao.maps.LatLng(37.566826, 126.9786567); // 기본값 (서울시청)
                }
                const mapModalElement = document.getElementById('mapModal');
                if (mapModalElement) {
                    const modalInstance = bootstrap.Modal.getOrCreateInstance(mapModalElement);
                    modalInstance.show(); // 'shown.bs.modal' 이벤트가 지도 중앙 재설정 처리
                }
            });
        } else {
            if (!selectedLatlng) selectedLatlng = new kakao.maps.LatLng(37.566826, 126.9786567);
            const mapModalElement = document.getElementById('mapModal');
            if (mapModalElement) {
                const modalInstance = bootstrap.Modal.getOrCreateInstance(mapModalElement);
                modalInstance.show();
            }
        }
    }

    if (aiLocationMapButton && aiLocationInput) {
        aiLocationMapButton.addEventListener('click', function () {
            // AI 추천 탭의 경우, 'aiLocationInput'의 현재 값을 지오코딩하여 초기 좌표로 사용하거나,
            // 'selectedLatlng'에 이미 값이 있다면 (예: 이전에 지도에서 선택) 그 값을 사용.
            let initialCoordsForAIMap = null;
            if (selectedLatlng && currentMapTargetInputId === 'aiLocationInput') { // 이전에 AI입력용으로 선택한 좌표가 있다면
                initialCoordsForAIMap = selectedLatlng;
            }
            // openMapModal은 initialCoords가 null이고 aiLocationInput.value가 있으면 지오코딩 시도
            openMapModal('aiLocationInput', initialCoordsForAIMap);
        });
    }

    if(aiLocationInput) {
        aiLocationInput.removeAttribute('readonly');
        aiLocationInput.addEventListener('blur', function() {
            const address = this.value;
            if (address && kakaoMapsApiIsReady && geocoder) {
                geocoder.addressSearch(address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK && result.length > 0) {
                        selectedLatlng = new kakao.maps.LatLng(result[0].y, result[0].x); // AI용 좌표 업데이트
                        console.log("AI 주소 직접 입력 검증:", result[0].address_name, "좌표:", selectedLatlng);
                        aiLocationInput.value = getSimpleAddressFromKakao(result[0]);
                    } else {
                        console.warn("AI 주소 직접 입력 검증 실패:", address);
                        // selectedLatlng = null; // 실패 시 null 처리 또는 기존 값 유지 선택
                    }
                });
            } else if (!address) {
                // selectedLatlng = null; // 입력값이 없으면 null 처리 또는 기존 값 유지 선택
            }
        });
    }

    if(manualLocationInput) { // 직접 검색 탭의 "지도에서 위치 선택" 입력 필드
        manualLocationInput.addEventListener('click', function () {
            // 직접 검색 탭의 경우, 'manualLocationInput'의 현재 값을 지오코딩하여 초기 좌표로 사용하거나,
            // 'directSearchSelectedLatlng'에 값이 있다면 (이전에 직접 검색용으로 지도에서 선택) 그 값을 사용.
            let initialCoordsForManualMap = null;
            if (directSearchSelectedLatlng) {
                initialCoordsForManualMap = directSearchSelectedLatlng;
            }
            // openMapModal은 initialCoords가 null이고 manualLocationInput.value가 있으면 지오코딩 시도
            openMapModal('manualLocationInput', initialCoordsForManualMap);
        });

        // manualLocationInput에 직접 주소 입력 후 blur 시 처리 (선택 사항)
        manualLocationInput.addEventListener('blur', function() {
            const address = this.value;
            if (address && kakaoMapsApiIsReady && geocoder) {
                geocoder.addressSearch(address, function(result, status) {
                    if (status === kakao.maps.services.Status.OK && result.length > 0) {
                        directSearchSelectedLatlng = new kakao.maps.LatLng(result[0].y, result[0].x); // 직접 검색용 좌표 업데이트
                        console.log("수동 검색 주소 직접 입력 검증:", result[0].address_name, "좌표:", directSearchSelectedLatlng);
                        manualLocationInput.value = getSimpleAddressFromKakao(result[0]);
                    } else {
                        console.warn("수동 검색 주소 직접 입력 검증 실패:", address);
                        // directSearchSelectedLatlng = null; // 실패 시 기존 좌표 유지 또는 null 처리 선택
                    }
                });
            } else if (!address) {
                // directSearchSelectedLatlng = null; // 입력값이 없으면 좌표도 null 처리 선택
            }
        });
    }


    const searchMapButton = document.getElementById('searchMapButton'); // 모달 내 '검색' 버튼
    if (searchMapButton) {
        searchMapButton.addEventListener('click', function () {
            if (!kakaoMapsApiIsReady) {
                alert("지도 서비스가 준비 중입니다. 잠시 후 다시 시도해주세요.");
                return;
            }
            const keywordVal = document.getElementById('keywordMapInput').value;
            if (!keywordVal.trim()) {
                alert('키워드를 입력해주세요!');
                return;
            }
            const ps = new kakao.maps.services.Places();
            ps.keywordSearch(keywordVal, placesSearchCB);
        });
    }
    document.getElementById('keywordMapInput')?.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            searchMapButton?.click();
        }
    });


    if (confirmLocationButton) {
        confirmLocationButton.addEventListener('click', function () {
            const modalElement = document.getElementById('mapModal');
            const modalInstance = modalElement ? bootstrap.Modal.getInstance(modalElement) : null;

            if (currentMapTargetInputId && selectedLatlng) { // selectedLatlng은 지도 클릭/검색 시 업데이트된 최신 좌표
                const targetInput = document.getElementById(currentMapTargetInputId);
                if (targetInput) {
                    // searchDetailAddrFromCoords를 호출하여 targetInput.value를 최종 업데이트
                    searchDetailAddrFromCoords(selectedLatlng, function(result, status) {
                        let finalAddress = "주소 변환 실패";
                        if (status === kakao.maps.services.Status.OK && result.length > 0) {
                            finalAddress = getSimpleAddressFromKakao(result[0]);
                        } else {
                            // 이미 selectedLatlng 기반으로 주소가 입력되어 있을 수 있으므로, 실패 시 강제로 비우지는 않음.
                            // 필요하다면 targetInput.value = ''; 등으로 처리 가능
                            console.error("최종 주소 변환 실패:", status);
                            // 기존에 입력된 값이 있다면 그것을 유지하거나, "주소 변환 실패"와 같은 메시지를 표시할 수 있음
                            // 현재는 getSimpleAddressFromKakao가 실패하면, targetInput.value가 이전 값(infowindow 표시 값)을 유지할 가능성 있음.
                        }
                        targetInput.value = finalAddress; // 최종 변환된 주소로 업데이트

                        // 각 탭에 맞는 좌표 변수에 저장
                        if (currentMapTargetInputId === 'manualLocationInput') {
                            directSearchSelectedLatlng = new kakao.maps.LatLng(selectedLatlng.getLat(), selectedLatlng.getLng()); // 새 객체로 복사하여 할당
                            console.log("수동 검색 위치 확정:", directSearchSelectedLatlng, targetInput.value);
                        } else if (currentMapTargetInputId === 'aiLocationInput') {
                            // selectedLatlng은 이미 AI 추천용으로 사용될 것이므로 별도 변환 없이 사용
                            console.log("AI 추천 위치 확정:", selectedLatlng, targetInput.value);
                        }
                    });
                }
            } else if (currentMapTargetInputId && !selectedLatlng) {
                // 이 경우는 거의 발생하지 않아야 함 (지도를 열면 기본 위치라도 selectedLatlng이 설정됨)
                // 만약 발생한다면, 입력 필드를 비우거나 기본값 처리
                const targetInput = document.getElementById(currentMapTargetInputId);
                if(targetInput) targetInput.value = ''; // 또는 기본 안내 메시지
                if (currentMapTargetInputId === 'manualLocationInput') {
                    directSearchSelectedLatlng = null;
                }
                console.warn("위치 확정 시 selectedLatlng이 없습니다. 입력 필드가 비워질 수 있습니다.");
            }


            if (modalInstance) {
                modalInstance.hide();
            }
            // currentMapTargetInputId = null; // 모달이 닫힐 때 자동으로 null 처리되거나, 다음 열릴 때 설정됨
            // selectedLatlng은 다음 지도 인터랙션을 위해 유지될 수 있음 (AI, 수동 검색 간 전환 시 마지막 위치 기억 등)
            // 또는 null로 초기화: selectedLatlng = null;
        });
    }

    const petForm = document.getElementById('petForm');
    if (petForm) {
        petForm.addEventListener('submit', function (event) {
            event.preventDefault();
            const spinner = document.getElementById('spinnerGlobalAi');
            const resultsContainer = document.getElementById('aiRecommendResults');
            resultsContainer.innerHTML = '';
            if(spinner) spinner.style.display = 'flex';


            const species = document.getElementById('petSpecies').value;
            const petSizeChecked = document.querySelector('input[name="petSizeAI"]:checked');
            const weight = petSizeChecked ? petSizeChecked.value : '';
            const isPredatorChecked = document.querySelector('input[name="isPredatorAI"]:checked');
            const is_danger = isPredatorChecked ? (isPredatorChecked.value === '예') : false;
            const location = document.getElementById('aiLocationInput').value;
            const info = document.getElementById('petInfo').value;

            // AI 추천 요청 시 사용될 좌표는 selectedLatlng (aiLocationInput과 연동된)
            const currentAiLat = selectedLatlng ? selectedLatlng.getLat() : null;
            const currentAiLng = selectedLatlng ? selectedLatlng.getLng() : null;


            const requestData = {
                species: document.getElementById('petSpecies').value,
                weight: document.querySelector('input[name="petSizeAI"]:checked')?.value || '',
                is_danger: (document.querySelector('input[name="isPredatorAI"]:checked')?.value === '예') || false,
                location: document.getElementById('aiLocationInput').value,
                info: document.getElementById('petInfo').value,
            };

            console.log("AI Recommend Request:", requestData);

            setTimeout(() => { // 이 부분이 실제 fetch().then().then() 콜백이라고 가정
                if(spinner) spinner.style.display = 'none';

                // 예시 RecommendResponseDTO 구조 (실제 API 응답을 여기에 맞춰야 함)
                const mockRecommendResponseDTO = {
                    recommend: [
                        { contentId: "123", title: "행복한강아지공원 (수동입력결과)", addr: "서울 강남구", description: "넓고 깨끗한 공원입니다.", imageUrl: "/assets/noimg.png", acmpyTypeCd: "모두가능", acmpyPsblCpam: "소형견, 중형견", acmpyNeedMtr: "목줄필수", recommendReason: "사용자 입력 기반 AI 추천입니다." },
                        { contentId: "789", title: "멍멍이카페 (수동입력결과)", addr: "서울 마포구", description: "다양한 간식이 있어요.", imageUrl: null, acmpyTypeCd: "실내가능", acmpyPsblCpam: "소형견", acmpyNeedMtr: "기본 매너", recommendReason: "실내 활동을 선호하는 사용자에게 추천합니다." }
                    ]
                };

                lastManualAiRecommendResponse = mockRecommendResponseDTO; // 전체 DTO 저장

                if (resultsContainer) {
                    // displayAiRecommendations는 이전 답변에서 recommendReason 및 링크 수정된 버전으로 가정
                    displayAiRecommendations(mockRecommendResponseDTO.recommend);
                    // 파이프라인 결과 페이지 이동 버튼 추가
                    addGoToPipelineResultPageButton(resultsContainer, lastManualAiRecommendResponse);
                } else {
                    console.error("AI results container not found.");
                }
            }, 1000);
        });
    }
    // 새 함수: 파이프라인 결과 페이지 이동 버튼 추가
    function addGoToPipelineResultPageButton(resultsDisplayArea, recommendationData) {
        if (!resultsDisplayArea || !recommendationData || !recommendationData.recommend || recommendationData.recommend.length === 0) {
            console.log("추천 데이터가 없거나 결과 표시 영역이 없어 버튼을 추가하지 않습니다.");
            return;
        }

        // 기존 버튼 제거 (중복 방지)
        let existingButtonContainer = resultsDisplayArea.parentElement.querySelector('.go-to-pipeline-button-container');
        if (existingButtonContainer) {
            existingButtonContainer.remove();
        }

        const buttonContainer = document.createElement('div');
        buttonContainer.className = 'text-center mt-4 mb-3 go-to-pipeline-button-container';

        const button = document.createElement('button');
        button.className = 'btn btn-outline-info';
        button.innerHTML = '<i class="bi bi-card-list me-2"></i>이 추천결과를 사진 분석 스타일 페이지에서 보기';
        button.type = 'button';

        button.addEventListener('click', async () => {
            button.disabled = true;
            button.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> 세션에 저장 중...';

            try {
                const response = await fetch('/api/session/recommendation', { // 새 API 엔드포인트
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(recommendationData) // 저장된 RecommendResponseDTO 전달
                });

                if (response.ok) {
                    // 세션 저장 성공 시, 파이프라인 결과 페이지로 이동
                    window.location.href = '/flow/showRecommendationResult';
                } else {
                    const errorText = await response.text();
                    pipelineJsonStatus.innerHTML = `<div class="alert alert-danger mt-2">세션 저장 실패: ${errorText}</div>`; // 상태 표시 (pipelineJsonStatus가 있다면 활용)
                    console.error('세션 저장 실패:', errorText);
                    alert('세션 저장에 실패했습니다: ' + errorText);
                    button.disabled = false;
                    button.innerHTML = '<i class="bi bi-card-list me-2"></i>이 추천결과를 사진 분석 스타일 페이지에서 보기';
                }
            } catch (error) {
                pipelineJsonStatus.innerHTML = `<div class="alert alert-danger mt-2">요청 중 오류 발생: ${error.message}</div>`; // 상태 표시
                console.error('세션 저장 요청 오류:', error);
                alert('요청 중 오류가 발생했습니다: ' + error.message);
                button.disabled = false;
                button.innerHTML = '<i class="bi bi-card-list me-2"></i>이 추천결과를 사진 분석 스타일 페이지에서 보기';
            }
        });

        buttonContainer.appendChild(button);
        // aiRecommendResults div 다음에 버튼 컨테이너 삽입
        resultsDisplayArea.parentNode.insertBefore(buttonContainer, resultsDisplayArea.nextSibling);
    }



    const directSearchForm = document.getElementById('directSearchForm');
    const useCurrentLocationSwitch = document.getElementById('useCurrentLocationSwitch');
    const areaSigunguFiltersDiv = document.getElementById('areaSigunguFilters');
    const currentLocationFiltersDiv = document.getElementById('currentLocationFilters');
    const directSearchAreaCodeSelect = document.getElementById('directSearchAreaCode');
    const directSearchSigunguCodeSelect = document.getElementById('directSearchSigunguCode');
    const loadMoreButtonDirectSearch = document.getElementById('loadMoreButtonDirectSearch');



    function toggleLocationFilterUI(isCurrentLocationActive) {
        if (!areaSigunguFiltersDiv || !currentLocationFiltersDiv) {
            console.error("위치 필터 DIV 요소를 찾을 수 없습니다. ID를 확인하세요.");
            return;
        }

        if (isCurrentLocationActive) {
            areaSigunguFiltersDiv.classList.add('d-none');
            currentLocationFiltersDiv.classList.remove('d-none');

            if (directSearchAreaCodeSelect) directSearchAreaCodeSelect.disabled = true;
            if (directSearchSigunguCodeSelect) directSearchSigunguCodeSelect.disabled = true;
        } else {
            areaSigunguFiltersDiv.classList.remove('d-none');
            currentLocationFiltersDiv.classList.add('d-none');

            if (directSearchAreaCodeSelect) directSearchAreaCodeSelect.disabled = false;
            if (directSearchSigunguCodeSelect) {
                directSearchSigunguCodeSelect.disabled = !directSearchAreaCodeSelect.value;
            }
            // "현재 위치 기반 검색" 해제 시, 수동 검색용 위치 정보 초기화 (선택적)
            // if (manualLocationInput) manualLocationInput.value = '';
            // directSearchSelectedLatlng = null;
            // console.log("현재 위치 기반 검색 비활성화: 수동 선택 위치 정보 초기화됨 (선택적).");
        }
    }


    if (useCurrentLocationSwitch) {
        toggleLocationFilterUI(useCurrentLocationSwitch.checked); // 페이지 로드 시 초기 상태 설정
        useCurrentLocationSwitch.addEventListener('change', function () {
            toggleLocationFilterUI(this.checked);
            if (!this.checked) { // "현재 위치 기반 검색" 스위치가 꺼졌을 때
                // 선택된 지도 위치 정보 초기화 (사용자 혼란 방지)
                if (manualLocationInput) {
                    manualLocationInput.value = ''; // 입력 필드 초기화
                }
                directSearchSelectedLatlng = null; // 저장된 좌표 초기화
                console.log("현재 위치 기반 검색 비활성화: 선택된 위치 정보 초기화됨.");
            }
        });
    } else {
        console.warn("'useCurrentLocationSwitch' 요소를 찾을 수 없습니다.");
    }


    if(directSearchAreaCodeSelect) {
        loadAreaCodes();
        directSearchAreaCodeSelect.addEventListener('change', function () {
            const selectedAreaCode = this.value;
            loadSigunguCodes(selectedAreaCode);
        });

    }

    if (directSearchForm) {
        directSearchForm.addEventListener('submit', function (event) {
            event.preventDefault();
            directSearchCurrentPage = 0; // 새 검색이므로 첫 페이지부터
            const resultList = document.getElementById('directSearchResultList');
            if(resultList) resultList.innerHTML = ''; // 이전 결과 지우기

            // 로딩 스피너 및 결과 컨트롤 초기화
            const spinner = document.getElementById('spinnerDirectSearch');
            const resultsControls = document.getElementById('resultControlsDirectSearch');
            if(spinner) spinner.style.display = 'block'; // fetch 하기 전에 스피너 표시
            if(resultsControls) resultsControls.style.display = 'none';
            fetchDirectSearchResults(false); // append = false
        });
    }

    if(loadMoreButtonDirectSearch){
        loadMoreButtonDirectSearch.addEventListener('click', function(){
            directSearchCurrentPage++; // 다음 페이지로 증가
            fetchDirectSearchResults(true); // append = true
        });
    }

}); // End DOMContentLoaded


function searchDetailAddrFromCoords(coords, callback) {
    if (!kakaoMapsApiIsReady || !geocoder) {
        console.error("Geocoder not ready for searchDetailAddrFromCoords");
        if (callback) callback(null, kakao.maps.services.Status.ERROR);
        return;
    }
    geocoder.coord2Address(coords.getLng(), coords.getLat(), callback);
}

function placesSearchCB(data, status, pagination) {
    if (!kakaoMapsApiIsReady || !map || !marker || !infowindow) {
        console.error("Map components not ready for placesSearchCB");
        return;
    }
    if (status === kakao.maps.services.Status.OK) {
        const firstPlace = data[0];
        if (firstPlace) {
            selectedLatlng = new kakao.maps.LatLng(firstPlace.y, firstPlace.x);
            map.setCenter(selectedLatlng);
            marker.setPosition(selectedLatlng);

            const placeName = firstPlace.place_name;
            const addressName = firstPlace.road_address_name || firstPlace.address_name; // 도로명 우선, 없으면 지번
            const content = `<div class="petty-map-infowindow">
                               <div class="info-title">${placeName}</div>
                               <div class="info-address">${addressName || '주소 정보 없음'}</div>
                             </div>`;
            infowindow.setContent(content);
            infowindow.open(map, marker);

            if (currentMapTargetInputId) {
                const targetInput = document.getElementById(currentMapTargetInputId);
                if (targetInput) {
                    targetInput.value = getSimpleAddressFromKakao(firstPlace); // Places API 결과도 getSimpleAddressFromKakao 사용
                }
            }
        } else {
            alert('검색 결과가 없습니다.');
        }
    } else if (status === kakao.maps.services.Status.ZERO_RESULT) {
        alert('검색 결과가 존재하지 않습니다.');
    } else {
        alert('키워드 검색 중 오류가 발생했습니다: ' + status);
    }
}

function displayAiRecommendations(recommends) {
    const container = document.getElementById('aiRecommendResults');
    container.innerHTML = '';

    if (!recommends || recommends.length === 0) {
        container.innerHTML = '<p class="text-center text-muted col-12 mt-4">AI 추천 결과가 없습니다.</p>';
        return;
    }

    recommends.forEach(rec => {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('ai-recommend-item'); // mb-3은 recommend.css 에서 .ai-recommend-item 에 이미 포함됨

        const altText = rec.title || '추천 장소 이미지';
        const actualImageUrl = rec.imageUrl || '/assets/noimg.png'; // 이미지가 없을 경우 로컬 noimg.png 사용

        let imageHtml = `<img src="${actualImageUrl}" alt="${altText}" class="img-fluid item-image mb-2 rounded">`;



        const descriptionHtml = rec.description ? rec.description.replace(/</g, "&lt;").replace(/>/g, "&gt;") : '이 장소는 사용자의 요청에 부합할 수 있습니다.';
        const detailLink = rec.contentId ? `/contents/${rec.contentId}` : '#!';

        itemDiv.innerHTML = `
            ${imageHtml}
            <h5 class="item-title">${rec.title || '제목 없음'}</h5>
            <p class="item-address"><i class="bi bi-geo-alt-fill me-1 text-secondary"></i>${rec.addr || '주소 정보 없음'}</p>
            <div class="item-ai-description">
                <strong><i class="bi bi-lightbulb me-1"></i>추천 이유:</strong> ${descriptionHtml}
            </div>
            <div class="item-pet-info mt-2 small">
                ${rec.acmpyTypeCd ? `<p class="mb-1"><strong>동반 유형:</strong> ${rec.acmpyTypeCd}</p>` : ''}
                ${rec.acmpyPsblCpam ? `<p class="mb-1"><strong>동반 가능 동물:</strong> ${rec.acmpyPsblCpam}</p>` : ''}
                ${rec.acmpyNeedMtr ? `<p class="mb-0"><strong>필요 사항:</strong> ${rec.acmpyNeedMtr}</p>` : ''}
            </div>
            <div class="mt-3 text-end">
                <a href="${detailLink}" target="_blank" class="btn btn-sm btn-outline-primary ${!rec.contentId ? 'disabled' : ''}">상세보기</a>
            </div>`;
        container.appendChild(itemDiv);
    });
}

function getSimpleAddressFromKakao(kakaoResult) {
    let addressName = '';
    // Geocoder 결과 (coord2Address)
    if (kakaoResult && kakaoResult.road_address && kakaoResult.road_address.address_name) {
        addressName = kakaoResult.road_address.address_name;
    } else if (kakaoResult && kakaoResult.address && kakaoResult.address.address_name) {
        addressName = kakaoResult.address.address_name;
    }
    // Places 결과 (keywordSearch)
    else if (kakaoResult && kakaoResult.road_address_name) { // Places API는 road_address_name을 직접 제공
        addressName = kakaoResult.road_address_name;
    } else if (kakaoResult && kakaoResult.address_name) { // Places API는 address_name을 직접 제공
        addressName = kakaoResult.address_name;
    }


    if (!addressName) return '주소 정보 없음';

    // 예시: "서울 강남구", "경기 수원시 장안구", "세종특별자치시", "제주특별자치도 제주시"
    // "시/도 구/군" 또는 "시/도 시/군" 까지만 표시하거나, 더 간단하게
    const parts = addressName.split(' ');
    if (parts.length > 2) {
        // "특별자치시", "광역시", "특별시", "도", "특별자치도"로 끝나는 첫번째 부분
        if (parts[0].match(/(특별자치시|광역시|특별시|도|특별자치도)$/)) {
            if (parts[1].match(/(시|군|구)$/)) { // 그 다음이 시/군/구 이면
                return `${parts[0]} ${parts[1]}`; // 예: "경기 수원시", "제주 제주시" (원래는 "제주특별자치도 제주시")
                // "세종특별자치시"는 parts[1]이 없으므로 아래에서 처리
            }
            // 세종특별자치시 같은 경우 parts[0]만 반환되도록
            if (parts.length === 1 || !parts[1].match(/(시|군|구)$/)) {
                return parts[0]; // "세종특별자치시"
            }
        }
        // 일반적인 경우: "시/도 구/군" (e.g., 서울 강남구) 또는 "시/도 시 구" (e.g., 경기 수원시 장안구)
        // "시/도" + "시/군/구" (e.g., 경기 수원시)
        if (parts[0] && parts[1]) {
            if (parts[1].endsWith('시') && parts.length > 2 && parts[2].endsWith('구')) { // ex: 수원시 장안구
                return `${parts[0]} ${parts[1]} ${parts[2]}`;
            }
            return `${parts[0]} ${parts[1]}`;
        }
    }
    return addressName; // 2부분 이하이거나 특별 케이스에 해당하지 않으면 원본 반환
}