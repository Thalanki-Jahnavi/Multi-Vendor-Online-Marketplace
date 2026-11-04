document.addEventListener("DOMContentLoaded", () => {
    const menuToggle = document.getElementById("menuToggle");
    const navigation = document.getElementById("mainNavigation");

    if (menuToggle && navigation) {
        menuToggle.addEventListener("click", () => {
            const isOpen = navigation.classList.toggle("open");
            menuToggle.setAttribute("aria-expanded", String(isOpen));
            menuToggle.setAttribute("aria-label", isOpen ? "Close navigation" : "Open navigation");
        });
    }

    const role = (document.body.dataset.role || "GUEST").toUpperCase();
    document.body.dataset.role = role;

    const cartKey = "onecart-cart";
    const wishlistKey = "onecart-wishlist";

    const readList = (key) => {
        try {
            const value = JSON.parse(localStorage.getItem(key) || "[]");
            return Array.isArray(value) ? value : [];
        } catch {
            return [];
        }
    };

    const saveList = (key, value) => localStorage.setItem(key, JSON.stringify(value));

    const updateCounts = () => {
        document.getElementById("cartCount").textContent = readList(cartKey).length;
        document.getElementById("wishlistCount").textContent = readList(wishlistKey).length;
    };

    document.querySelectorAll(".add-cart-button").forEach((button) => {
        button.addEventListener("click", () => {
            const id = button.dataset.productId;
            const cart = readList(cartKey);

            if (!cart.some((item) => String(item.id) === String(id))) {
                cart.push({
                    id,
                    name: button.dataset.productName || "Product",
                    quantity: 1
                });
                saveList(cartKey, cart);
            }

            updateCounts();
            button.textContent = "Added to cart ✓";
            window.setTimeout(() => button.textContent = "Add to cart", 1300);
        });
    });

    document.querySelectorAll(".wishlist-button").forEach((button) => {
        const id = button.dataset.productId;
        const saved = readList(wishlistKey).some((item) => String(item.id) === String(id));
        button.classList.toggle("is-saved", saved);
        button.textContent = saved ? "♥" : "♡";

        button.addEventListener("click", () => {
            let wishlist = readList(wishlistKey);
            const exists = wishlist.some((item) => String(item.id) === String(id));

            wishlist = exists
                ? wishlist.filter((item) => String(item.id) !== String(id))
                : [...wishlist, { id }];

            saveList(wishlistKey, wishlist);
            button.classList.toggle("is-saved", !exists);
            button.textContent = exists ? "♡" : "♥";
            updateCounts();
        });
    });

    const searchForm = document.getElementById("searchForm");
    const searchInput = document.getElementById("productSearch");
    const categorySelect = document.getElementById("categorySelect");
    const productCards = [...document.querySelectorAll(".product-card")];

    // On the homepage, filter the products currently rendered by Thymeleaf.
    // Submitting the search form can also navigate to /products?q=...&category=...
    const filterProducts = () => {
        const query = (searchInput.value || "").trim().toLowerCase();
        const category = (categorySelect.value || "").trim().toLowerCase();

        productCards.forEach((card) => {
            const name = (card.dataset.name || "").toLowerCase();
            const cardCategory = (card.dataset.category || "").toLowerCase();
            const matchesQuery = !query || name.includes(query);
            const matchesCategory = !category || cardCategory === category;
            card.hidden = !(matchesQuery && matchesCategory);
        });
    };

    searchInput?.addEventListener("input", filterProducts);
    categorySelect?.addEventListener("change", filterProducts);

    searchForm?.addEventListener("submit", (event) => {
        if (productCards.length) {
            event.preventDefault();
            filterProducts();
            document.getElementById("featured")?.scrollIntoView({ behavior: "smooth" });
        }
    });

    document.getElementById("currentYear").textContent = new Date().getFullYear();

    document.getElementById("newsletterForm")?.addEventListener("submit", (event) => {
        event.preventDefault();
        const email = document.getElementById("newsletterEmail");
        document.getElementById("newsletterMessage").textContent =
            `Thanks! ${email.value} is entered. Newsletter signup needs a backend endpoint to save it.`;
        email.value = "";
    });

    updateCounts();
});