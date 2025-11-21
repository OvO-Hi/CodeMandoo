document.addEventListener("DOMContentLoaded", () => {
    const loading = document.getElementById("loadingScreen");
    const mainContent = document.querySelector("main");

    const prompt = localStorage.getItem("generatedPrompt");
    const imageUrl = localStorage.getItem("generatedImageUrl");

    document.getElementById("promptText").textContent =
        prompt || "프롬프트 정보가 없습니다.";

    const imageElement = document.getElementById("resultImage");
    imageElement.src = imageUrl || "";
    imageElement.style.display = imageUrl ? "block" : "none";

    loading.style.display = "none";
    mainContent.style.display = "flex";

    //처음으로 돌아가기 버튼 기능
    document.getElementById("goBackButton").addEventListener("click", () => {
        window.location.href = "form.html";
    });
});
