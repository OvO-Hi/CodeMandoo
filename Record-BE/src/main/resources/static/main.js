document.getElementById("submitButton").addEventListener("click", async function () {
    const form = document.getElementById("promptForm");
    const loading = document.getElementById("loadingScreen"); // 로딩화면 요소

    if (!form.checkValidity()) {
        form.reportValidity();
        return;
    }

    //로딩 화면 표시
    loading.style.display = "flex";

    const castInput = document.getElementById("cast").value.split(",").map(name => name.trim());

    const data = {
        title: document.getElementById("title").value,
        location: document.getElementById("location").value,
        date: document.getElementById("date").value,
        genre: document.getElementById("genre").value,
        cast: castInput,
        review: document.getElementById("review").value
    };

    try {
        const response = await fetch("http://localhost:8080/generate-image", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        localStorage.setItem("generatedPrompt", result.prompt);
        localStorage.setItem("generatedImageUrl", result.imageUrl);

        //결과 페이지로 이동
        window.location.href = "result.html";

    } catch (error) {
        alert("에러가 발생했습니다: " + error.message);
        //실패 시 로딩 화면 숨김
        loading.style.display = "none";
    }
});
