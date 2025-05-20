const API_BASE_URL = '/api/v1/contents'; // Your backend API base path
const resultsDiv = document.getElementById('results');
const loadingDiv = document.getElementById('loading');
const loadMoreBtn = document.getElementById('loadMoreBtn');
const areaSelect = document.getElementById('areaCode');
const sigunguSelect = document.getElementById('sigunguCode');

let currentPage = 0;
let totalPages = 0;
let currentSearchType = 'area'; // 'area' or 'location'
let isLoading = false;
let currentSearchParameters = {}; // Store parameters for loadMore

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
    // Set initial active tab
    switchSearchType('area');

    // Add event listener for area code changes to potentially load sigungu codes
    // (Requires a backend endpoint for sigungu codes)
    areaSelect.addEventListener('change', (e) => {
        const areaCode = e.target.value;
        sigunguSelect.innerHTML = '<option value="">시/군/구 선택</option>'; // Clear previous options
        sigunguSelect.disabled = true;
        if (areaCode) {
            loadSigunguCodes(areaCode); // Uncomment if you implement this
        }
    });
});

// --- Tab Switching ---
function switchSearchType(type) {
    currentSearchType = type;
    document.querySelectorAll('.tab').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.search-box').forEach(box => box.classList.remove('active'));

    document.getElementById(`tab-${type}`).classList.add('active');
    document.getElementById(`${type}Search`).classList.add('active');

    // Reset results when switching tabs
    resultsDiv.innerHTML = '';
    loadMoreBtn.style.display = 'none';
    currentPage = 0;
    totalPages = 0;
}

// --- Loading Indicator ---
function showLoading(show) {
    isLoading = show;
    loadingDiv.style.display = show ? 'block' : 'none';
}

// --- Get Current Location ---
function getCurrentLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                document.getElementById('mapY').value = position.coords.latitude.toFixed(6);
                document.getElementById('mapX').value = position.coords.longitude.toFixed(6);
            },
            (error) => {
                console.error("Error getting current location:", error);
                alert("현재 위치를 가져올 수 없습니다. 직접 입력하거나 권한을 확인해주세요.");
            },
            { enableHighAccuracy: true } // Optional: Improve accuracy
        );
    } else {
        alert("이 브라우저에서는 위치 정보 기능을 지원하지 않습니다.");
    }
}

// --- Fetch Data (Generic Helper) ---
async function fetchData(url) {
    showLoading(true);
    try {
        const response = await fetch(url);
        if (!response.ok) {
            // Try to get error message from backend response body
            let errorMsg = `Error: ${response.status} ${response.statusText}`;
            try {
                const errorData = await response.json();
                errorMsg = errorData.message || errorMsg; // Use backend message if available
            } catch(e) { /* Ignore if response is not JSON */ }
            throw new Error(errorMsg);
        }
        return await response.json();
    } catch (error) {
        console.error("API Call Failed:", error);
        displayError(`데이터 로딩 실패: ${error.message}`);
        return null; // Indicate failure
    } finally {
        showLoading(false);
    }
}

// --- Search Functions ---
async function searchByArea(page = 0) {
    const areaCode = areaSelect.value;
    const sigunguCode = sigunguSelect.value;
    // const contentTypeId = document.getElementById('areaContentTypeId')?.value; // Optional

    if (!areaCode) {
        alert("지역을 선택해주세요.");
        return;
    }

    // Store parameters for 'Load More'
    currentSearchParameters = { areaCode, sigunguCode }; // Add contentTypeId if used
    currentSearchType = 'area'; // Ensure type is set

    let url = `${API_BASE_URL}/search/area?areaCode=${areaCode}`;
    if (sigunguCode) {
        url += `&sigunguCode=${sigunguCode}`;
    }
    url += `&page=${page}&size=10`;
    // if (contentTypeId) {
    //     url += `&contentTypeId=${contentTypeId}`;
    // }

    const data = await fetchData(url);
    if (data) {
        displayResults(data, page > 0);
    } else {
        if (page === 0) resultsDiv.innerHTML = ''; // Clear if initial search failed
        loadMoreBtn.style.display = 'none';
    }
}

async function searchByLocation(page = 0) {
    const mapY = document.getElementById('mapY').value;
    const mapX = document.getElementById('mapX').value;
    const radius = document.getElementById('radius').value;
    // const contentTypeId = document.getElementById('locContentTypeId')?.value; // Optional

    if (!mapY || !mapX) {
        alert("위도와 경도를 입력하거나 현재 위치를 사용해주세요.");
        return;
    }
    if (!radius) {
        alert("검색 반경을 선택해주세요.");
        return;
    }

    // Store parameters for 'Load More'
    currentSearchParameters = { mapX, mapY, radius }; // Add contentTypeId if used
    currentSearchType = 'location'; // Ensure type is set

    let url = `${API_BASE_URL}/search/location?mapY=${mapY}&mapX=${mapX}&radius=${radius}&page=${page}&size=10`;
    // if (contentTypeId) {
    //     url += `&contentTypeId=${contentTypeId}`;
    // }

    const data = await fetchData(url);
    if (data) {
        displayResults(data, page > 0);
    } else {
        if (page === 0) resultsDiv.innerHTML = ''; // Clear if initial search failed
        loadMoreBtn.style.display = 'none';
    }
}

// --- Display Results ---
function displayResults(data, append = false) {
    if (!append) {
        resultsDiv.innerHTML = ''; // Clear previous results for a new search
    }

    if (!data || !data.content || data.content.length === 0) {
        if (!append) { // Only show 'no results' on the first page
            resultsDiv.innerHTML = '<p class="no-results">검색 결과가 없습니다.</p>';
        }
        loadMoreBtn.style.display = 'none';
        return;
    }

    // Update pagination info
    currentPage = data.page.number; // Spring Pageable is 0-indexed
    totalPages = data.page.totalPages;

    // Append new items
    data.content.forEach(item => {
        const itemDiv = document.createElement('div');
        itemDiv.className = 'result-item card'; // Use card class for better styling
        itemDiv.innerHTML = `
            ${item.firstImage ? `<img src="${item.firstImage}" alt="${item.title || '이미지'}" class="card-img-top" onerror="this.style.display='none'">` : '<div class="img-placeholder">이미지 없음</div>'}
            <div class="card-body">
                <h5 class="card-title">${item.title || '이름 없음'}</h5>
                <p class="card-text"><i class="fas fa-map-pin"></i> ${item.addr1 || '주소 정보 없음'}</p>
                ${item.distanceMeters ? `<p class="card-text"><i class="fas fa-road"></i> 약 ${Math.round(item.distanceMeters / 100) / 10}km</p>` : ''}
                <button class="btn btn-sm btn-primary detail-button" data-contentid="${item.contentId}">상세 정보</button>
                <div id="detail-${item.contentId}" class="detail-section" style="display: none;"></div>
            </div>
        `;
        resultsDiv.appendChild(itemDiv);

        // Add event listener for the detail button
        const detailButton = itemDiv.querySelector('.detail-button');
        detailButton.addEventListener('click', () => {
            showDetail(item.contentId);
        });
    });

    // Show or hide 'Load More' button
    if (currentPage < totalPages - 1) {
        loadMoreBtn.style.display = 'block';
    } else {
        loadMoreBtn.style.display = 'none';
    }
}

// --- Load More Results ---
function loadMore() {
    if (isLoading || currentPage >= totalPages - 1) {
        return; // Prevent multiple clicks or loading beyond the last page
    }
    const nextPage = currentPage + 1;
    if (currentSearchType === 'area') {
        searchByArea(nextPage);
    } else if (currentSearchType === 'location') {
        searchByLocation(nextPage);
    }
}

// --- Show/Hide Detail ---
async function showDetail(contentId) {
    const detailDiv = document.getElementById(`detail-${contentId}`);
    const detailButton = detailDiv.previousElementSibling; // Get the button

    if (!detailDiv) return;

    // Toggle visibility
    if (detailDiv.style.display === 'block') {
        detailDiv.style.display = 'none';
        detailButton.textContent = '상세 정보'; // Reset button text
    } else {
        // Show loading in detail section
        detailDiv.innerHTML = '<p>상세 정보 로딩 중...</p>';
        detailDiv.style.display = 'block';
        detailButton.textContent = '상세 정보 닫기'; // Change button text

        const url = `${API_BASE_URL}/${contentId}`;
        const detailData = await fetchData(url); // Use fetchData for consistency

        if (detailData) {
            renderDetail(detailDiv, detailData);
        } else {
            detailDiv.innerHTML = '<p>상세 정보를 불러오는데 실패했습니다.</p>';
            // Keep the div open to show the error
        }
    }
}

// --- Render Detail Information ---
function renderDetail(detailDiv, detailInfo) {
    if (!detailInfo) {
        detailDiv.innerHTML = "<p>상세 정보가 없습니다.</p>";
        return;
    }

    // Customize based on your DetailCommonDto structure
    let detailHtml = `<h6>상세 정보</h6>`;
    if (detailInfo.overview)
        detailHtml += `<p><strong>소개:</strong> ${detailInfo.overview}</p>`; // Assuming 'overview' field

    if (detailInfo.homepage)
        detailHtml += `<p><strong>홈페이지:</strong> <a href=${detailInfo.homepage}\</a></p>`;

    if (detailInfo.tel)
        detailHtml += `<p><strong>전화번호:</strong> ${detailInfo.tel}</p>`; // Assuming 'tel' field
    if (detailInfo.telname)
        detailHtml += `<p><strong>담당자:</strong> ${detailInfo.telname}</p>`; // Assuming 'tel' field

    detailHtml += `<h6><i class="fas fa-dog"></i> 반려동물 정보</h6>`;
    // Add fields from your old UI.renderDetail, checking if they exist in detailInfo
    const petFields = [
        { key: "acmpyPsblCpam", label: "동반 가능 동물" },
        { key: "acmpyTypeCd", label: "동반 유형" }, // Map code to text if needed
        { key: "acmpyNeedMtr", label: "동반 필요 조건" },
        { key: "relaPosesFclty", label: "관련 편의 시설" },
        { key: "relaFrnshPrdlst", label: "관련 비품 목록" },
        { key: "relaRntlPrdlst", label: "관련 대여 상품" },
        { key: "relaPurcPrdlst", label: "관련 구매 상품" },
        { key: "relaAcdntRiskMtr", label: "사고 예방 사항" },
        { key: "etcAcmpyInfo", label: "기타 동반 정보" },
    ];


    petFields.forEach(field => {
        const petValue = detailInfo.petTourInfo[field.key];
        if (petValue && petValue.trim() !== '') {
            detailHtml += `<p><strong>${field.label}:</strong> ${petValue}</p>`;
        }
    });

    detailDiv.innerHTML = detailHtml;
}

// --- Display Error ---
function displayError(message) {
    // Display error in the results area or a dedicated error div
    resultsDiv.innerHTML = `<p class="error-message">${message}</p>`;
    loadMoreBtn.style.display = 'none'; // Hide load more on error
}


// --- (Optional) Load Sigungu Codes ---
async function loadSigunguCodes(areaCode) {
    // IMPORTANT: Requires a backend endpoint like /api/v1/contents/codes/sigungu?areaCode={areaCode}
    const url = `${API_BASE_URL}/codes?areaCode=${areaCode}`;
    showLoading(true); // Indicate loading sigungu
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Failed to load sigungu codes: ${response.status}`);
        }
        const sigunguData = await response.json(); // Assuming backend returns List<SigunguCodeDto> with 'code' and 'name' fields

        sigunguSelect.innerHTML = '<option value="">시/군/구 선택</option>'; // Clear previous
        if (sigunguData && sigunguData.length > 0) {
            sigunguData.forEach(item => {
                const option = document.createElement('option');
                option.value = item.code; // Use the correct field name from your DTO
                option.textContent = item.name; // Use the correct field name from your DTO
                sigunguSelect.appendChild(option);
            });
            sigunguSelect.disabled = false;
        } else {
            sigunguSelect.disabled = true;
        }
    } catch (error) {
        console.error("Failed to load Sigungu codes:", error);
        sigunguSelect.disabled = true;
        // Optionally display an error message to the user
    } finally {
         showLoading(false); // Hide loading indicator used for sigungu
    }
}
