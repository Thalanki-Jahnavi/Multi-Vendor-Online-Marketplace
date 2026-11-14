document.addEventListener("DOMContentLoaded", function () {

    // Display current date
    const dateElement = document.getElementById("todayDate");

    if (dateElement) {
        const today = new Date();

        dateElement.textContent = today.toLocaleDateString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        });
    }

    // Mobile sidebar toggle
    const menuToggle = document.getElementById("menuToggle");
    const sidebar = document.getElementById("sidebar");

    if (menuToggle && sidebar) {

        menuToggle.addEventListener("click", function () {
            sidebar.classList.toggle("show");
        });

        // Close sidebar when clicking outside it on mobile
        document.addEventListener("click", function (event) {

            const clickedInsideSidebar = sidebar.contains(event.target);
            const clickedMenuButton = menuToggle.contains(event.target);

            if (!clickedInsideSidebar &&
                !clickedMenuButton &&
                window.innerWidth <= 760) {

                sidebar.classList.remove("show");
            }
        });
    }

});