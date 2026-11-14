
document.addEventListener("DOMContentLoaded", function () {

    // ==========================================
    // ELEMENT REFERENCES
    // ==========================================

    const searchInput = document.getElementById("guestSearch");
    const searchForm = document.getElementById("guestSearchForm");
    const sortSelect = document.getElementById("guestSort");
    const categoryList = document.getElementById("guestCategoryList");
    const productGrid = document.getElementById("guestProductGrid");
    const countElement = document.getElementById("guestProductCount");
    const noResults = document.getElementById("guestNoResults");

    const sidebarLinks = document.querySelectorAll(".guest-category-link");

    let selectedCategory = "all";

    // ==========================================
    // NORMALIZE TEXT
    // ==========================================

    function normalize(value) {
        return (value || "").trim().toLowerCase();
    }

    // Treat these category names as equivalent.
    function categoriesMatch(first, second) {

        const a = normalize(first);
        const b = normalize(second);

        if (a === b) {
            return true;
        }

        const homeCategories = ["home & kitchen", "home & living"];

        return homeCategories.includes(a) &&
               homeCategories.includes(b);
    }

    // ==========================================
    // CHECK PRODUCT GRID
    // ==========================================

    if (!productGrid || !categoryList) {
        console.warn("OneCart: Product grid or category list not found.");
        return;
    }

    const cards = Array.from(
        productGrid.querySelectorAll(".guest-product-card")
    );

    // ==========================================
    // BUILD CATEGORY FILTER BUTTONS
    // ==========================================

    function buildCategories() {

        const categories = new Set();

        cards.forEach(card => {

            const category = card.dataset.category;

            if (category && category.trim()) {
                categories.add(category.trim());
            }

        });

        // Remove old dynamic buttons but retain All.
        categoryList.querySelectorAll(
            '.category-filter:not([data-category="all"])'
        ).forEach(button => button.remove());

        Array.from(categories)
            .sort((a, b) => a.localeCompare(b))
            .forEach(category => {

                const button = document.createElement("button");

                button.type = "button";
                button.className = "category-filter";
                button.dataset.category = category;
                button.textContent = category;

                categoryList.appendChild(button);

            });

    }

    // ==========================================
    // SORT PRODUCTS
    // ==========================================

    function sortProducts() {

        if (!sortSelect) {
            return;
        }

        const sortValue = sortSelect.value;

        const sortedCards = [...cards];

        sortedCards.sort((a, b) => {

            const priceA = Number(a.dataset.price) || 0;
            const priceB = Number(b.dataset.price) || 0;

            const idA = Number(a.dataset.id) || 0;
            const idB = Number(b.dataset.id) || 0;

            if (sortValue === "price-low") {
                return priceA - priceB;
            }

            if (sortValue === "price-high") {
                return priceB - priceA;
            }

            return idB - idA;

        });

        sortedCards.forEach(card => {
            productGrid.appendChild(card);
        });

    }

    // ==========================================
    // APPLY SEARCH AND CATEGORY FILTERS
    // ==========================================

    function applyFilters() {

        const keyword = normalize(searchInput?.value);

        let visibleCount = 0;

        cards.forEach(card => {

            const name = normalize(card.dataset.name);
            const category = normalize(card.dataset.category);

            const vendor = normalize(
                card.querySelector(".guest-vendor-name")?.textContent
            );

            const searchMatch =
                !keyword ||
                name.includes(keyword) ||
                category.includes(keyword) ||
                vendor.includes(keyword);

            const categoryMatch =
                selectedCategory === "all" ||
                categoriesMatch(category, selectedCategory);

            const visible = searchMatch && categoryMatch;

            card.style.display = visible ? "" : "none";

            if (visible) {
                visibleCount++;
            }

        });

        // Update product count.
        if (countElement) {

            countElement.textContent =
                visibleCount +
                (visibleCount === 1 ? " product" : " products");

        }

        // Show empty search results.
        if (noResults) {

            noResults.style.display =
                visibleCount === 0 ? "block" : "none";

        }

    }

    // ==========================================
    // CATEGORY BUTTON CLICK
    // ==========================================

    categoryList.addEventListener("click", function (event) {

        const button = event.target.closest(".category-filter");

        if (!button) {
            return;
        }

        categoryList
            .querySelectorAll(".category-filter")
            .forEach(item => item.classList.remove("active"));

        button.classList.add("active");

        selectedCategory = button.dataset.category || "all";

        // Update sidebar active state.
        sidebarLinks.forEach(link => {

            link.classList.toggle(
                "active",
                categoriesMatch(link.dataset.category, selectedCategory) &&
                selectedCategory !== "all"
            );

        });

        applyFilters();

    });

    // ==========================================
    // SIDEBAR CATEGORY CLICK
    // ==========================================

    sidebarLinks.forEach(link => {

        link.addEventListener("click", function (event) {

            event.preventDefault();

            const category = this.dataset.category;

            if (!category) {
                return;
            }

            // Find the matching category button.
            const categoryButton = Array.from(
                categoryList.querySelectorAll(".category-filter")
            ).find(button => {

                return categoriesMatch(
                    button.dataset.category,
                    category
                );

            });

            // Select the category.
            if (categoryButton) {

                categoryButton.click();

            } else {

                // Still filter if no matching button exists.
                selectedCategory = category;

                categoryList
                    .querySelectorAll(".category-filter")
                    .forEach(button => button.classList.remove("active"));

                sidebarLinks.forEach(item => {

                    item.classList.toggle(
                        "active",
                        item === this
                    );

                });

                applyFilters();

            }

            // Scroll to products.
            const productsSection =
                document.getElementById("guestProducts");

            if (productsSection) {

                productsSection.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });

            }

        });

    });

    // ==========================================
    // SEARCH INPUT
    // ==========================================

    if (searchInput) {

        searchInput.addEventListener("input", function () {
            applyFilters();
        });

    }

    if (searchForm) {

        searchForm.addEventListener("submit", function (event) {

            event.preventDefault();

            applyFilters();

        });

    }

    // ==========================================
    // SORT CHANGE
    // ==========================================

    if (sortSelect) {

        sortSelect.addEventListener("change", function () {

            sortProducts();

            applyFilters();

        });

    }

    // ==========================================
    // RESET FILTERS
    // ==========================================

    window.resetGuestFilters = function () {

        if (searchInput) {
            searchInput.value = "";
        }

        selectedCategory = "all";

        if (sortSelect) {
            sortSelect.value = "newest";
        }

        categoryList
            .querySelectorAll(".category-filter")
            .forEach(button => {

                button.classList.toggle(
                    "active",
                    button.dataset.category === "all"
                );

            });

        sidebarLinks.forEach(link => {
            link.classList.remove("active");
        });

        sortProducts();

        applyFilters();

    };

    // ==========================================
    // INITIALIZE
    // ==========================================

    buildCategories();

    sortProducts();

    applyFilters();

});
