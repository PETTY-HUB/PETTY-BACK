let originalImages = [];
let postType = "QNA";

const postId = new URLSearchParams(location.search).get("id");

document.addEventListener("DOMContentLoaded", () => {
  fetchPostForEdit();

  document.getElementById("imageFiles").addEventListener("change", handleImageUpload);

  document.getElementById("postForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const token = localStorage.getItem("jwt");

    const payload = {
      title: document.getElementById("title").value,
      content: document.getElementById("content").value,
      petType: document.getElementById("petType").value || "OTHER",
      postType: postType,
      isResolved: getIsResolvedValue(),
      images: originalImages
    };



    const res = await fetch(`/api/posts/${postId}`, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      alert("수정 완료!");
      location.href = `/posts/detail?id=${postId}`;
    } else {
      alert("수정 실패");
    }
  });
});

async function fetchPostForEdit() {
  const res = await fetch(`/api/posts/${postId}`);
  const post = await res.json();

  document.getElementById("title").value = post.title;
  document.getElementById("content").value = post.content;

  const petTypeInputs = document.querySelectorAll('input[name="petType"]');
  petTypeInputs.forEach(input => {
    if (input.value === post.petType) input.checked = true;
  });

  const resolvedSelect = document.getElementById("isResolved");
  if (resolvedSelect) {
    resolvedSelect.value = String(post.isResolved);
  }

  const previewBox = document.getElementById("imagePreview");
  (post.images || []).forEach((img, index) => {
    const imgWrapper = document.createElement("div");
    imgWrapper.innerHTML = `
      <img src="${img.imageUrl}" data-url="${img.imageUrl}" style="max-width: 100px;">
      <button type="button" onclick="removeImage('${img.imageUrl}')">삭제</button>
    `;
    previewBox.appendChild(imgWrapper);

    originalImages.push({
      id: img.id,
      imageUrl: img.imageUrl,
      ordering: img.ordering,
      isDeleted: false
    });
  });
}

function getIsResolvedValue() {
  const select = document.getElementById("isResolved");
  return select ? select.value === "true" : false;
}

async function handleImageUpload(e) {
  const files = Array.from(e.target.files);
  if (!files.length) return;

  const currentCount = originalImages.filter(img => !img.isDeleted).length;
  const maxCount = 5;
  if (currentCount >= maxCount) {
    alert("최대 5개의 이미지를 업로드할 수 있습니다.");
    return;
  }

  const availableSlots = maxCount - currentCount;
  const filesToUpload = files.slice(0, availableSlots);

  const formData = new FormData();
  for (const file of filesToUpload) {
    formData.append("files", file);
  }

  const token = localStorage.getItem("jwt");
  const res = await fetch('/api/images/upload/multi', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    },
    body: formData
  });

  const json = await res.json();
  const previewBox = document.getElementById("imagePreview");

  json.images.forEach((img) => {
    if (originalImages.some(existing => existing.imageUrl === img.imageUrl)) return;

    originalImages.push(img);

    const imgWrapper = document.createElement("div");
    imgWrapper.innerHTML = `
      <img src="${img.imageUrl}" data-url="${img.imageUrl}" style="max-width: 100px;">
      <button type="button" onclick="removeImage('${img.imageUrl}')">삭제</button>
    `;
    previewBox.appendChild(imgWrapper);
  });
}

function removeImage(url) {
  const img = originalImages.find(img => img.imageUrl === url);
  if (img) {
    img.isDeleted = true;
  }

  const wrapper = document.querySelector(`img[data-url='${url}']`)?.parentElement;
  if (wrapper) wrapper.remove();
}
