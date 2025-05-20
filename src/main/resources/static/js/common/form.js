let uploadedImages = [];

document.getElementById('imageFiles').addEventListener('change', async (e) => {
  const MAX_FILE_COUNT = 5;
  const MAX_FILE_SIZE_MB = 5;

  const files = Array.from(e.target.files);

  if (files.length > MAX_FILE_COUNT) {
    alert(`이미지는 최대 ${MAX_FILE_COUNT}장까지만 업로드할 수 있습니다.`);
    e.target.value = ''; // 파일 선택 초기화
    return;
  }

  for (const file of files) {
    if (file.size > MAX_FILE_SIZE_MB * 1024 * 1024) {
      alert(`파일 ${file.name}은(는) 5MB를 초과합니다.`);
      e.target.value = '';
      return;
    }
  }

  const formData = new FormData();
  for (const file of files) {
    formData.append('files', file);
  }

  const token = localStorage.getItem('jwt');
  const res = await fetch('/api/images/upload/multi', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });

  const json = await res.json();

  if (!res.ok) {
    alert("이미지 업로드 실패: " + json.message);
    return;
  }

  if (!json.images || !Array.isArray(json.images)) {
    alert("이미지 응답이 잘못되었습니다");
    return;
  }

  for (let img of json.images) {
    console.log("업로드된 이미지:", img.imageUrl);
  }

  uploadedImages.push(...json.images); // imageUrl, ordering, isDeleted 포함
  // 알림 제거함

  const previewContainer = document.querySelector(".image-upload");

  // 기존 이미지 미리보기 제거
  const oldPreview = document.getElementById("imagePreview");
  if (oldPreview) oldPreview.remove();

  // 새로 미리보기 컨테이너 생성
  const previewDiv = document.createElement("div");
  previewDiv.id = "imagePreview";
  previewDiv.style.display = "flex";
  previewDiv.style.flexWrap = "wrap";
  previewDiv.style.gap = "10px";
  previewDiv.style.marginTop = "10px";

  // 이미지들 추가
  for (const img of json.images) {
    const imageEl = document.createElement("img");
    imageEl.src = img.imageUrl;
    imageEl.alt = "미리보기 이미지";
    imageEl.style.width = "100px";
    imageEl.style.borderRadius = "6px";
    imageEl.style.objectFit = "cover";
    previewDiv.appendChild(imageEl);
  }

  previewContainer.appendChild(previewDiv);
});

// 🔸 게시글 form 제출
document.getElementById('postForm').addEventListener('submit', async (e) => {
  e.preventDefault();

  const postData = {
    title: document.getElementById('title').value,
    content: document.getElementById('content').value,
    petType: document.getElementById('petType')?.value || getRadioValue('petType'),
    petName: document.getElementById('petName')?.value || null,
    region: document.getElementById('region')?.value || null,
    postType: detectPostType(),
    isResolved: false,
    images: uploadedImages // ✅ 새 필드!
  };

  const token = localStorage.getItem('jwt');
  const res = await fetch('/api/posts', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(postData)
  });

  if (res.ok) {
    const { id } = await res.json();
    alert('등록 완료!');
    location.href = `/posts/detail?id=${id}`;
  } else {
    alert('등록 실패 😢');
  }
});

// 🔸 라디오 버튼 값 추출 유틸
function getRadioValue(name) {
  const radios = document.querySelectorAll(`input[name="${name}"]`);
  for (const radio of radios) {
    if (radio.checked) return radio.value;
  }
  return null;
}

// 🔸 postType 자동 감지
function detectPostType() {
  if (location.pathname.includes('review')) return 'REVIEW';
  if (location.pathname.includes('qna')) return 'QNA';
  if (location.pathname.includes('showoff')) return 'SHOWOFF';
  return 'REVIEW'; // 기본값
}