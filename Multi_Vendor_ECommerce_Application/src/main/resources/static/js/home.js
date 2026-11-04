document.addEventListener("DOMContentLoaded", function () {

    const searchForm = document.querySelector(".search-form");
    const searchInput = document.getElementById("productSearch");
    const productCards = document.querySelectorAll(".product-card");

    searchForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const searchTerm = searchInput.value
            .trim()
            .toLowerCase();

        let visibleCount = 0;

        productCards.forEach(function (card) {

            const productName = card
                .querySelector("h3")
                .textContent
                .toLowerCase();

            const vendorName = card
                .querySelector(".vendor-name")
                .textContent
                .toLowerCase();

            const matches =
                productName.includes(searchTerm) ||
                vendorName.includes(searchTerm);

            card.style.display = matches ? "" : "none";

            if (matches) {
                visibleCount++;
            }
        });

        const countElement = document.querySelector(".product-count");

        if (countElement) {
            countElement.textContent =
                visibleCount + " products";
        }
    });

});