document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".toggle-password").forEach(function (button) {
        button.addEventListener("click", function () {
            const input = document.getElementById(button.dataset.password);
            if (!input) return;

            const show = input.type === "password";
            input.type = show ? "text" : "password";
            button.textContent = show ? "Hide" : "Show";
        });
    });

    const roleSelect = document.getElementById("role");
    const businessGroup = document.getElementById("businessNameGroup");
    const businessInput = document.getElementById("businessName");

    function updateBusinessField() {
        if (!roleSelect || !businessGroup || !businessInput) return;

        const isVendor = roleSelect.value === "VENDOR";
        businessGroup.hidden = !isVendor;
        businessInput.required = isVendor;

        if (!isVendor) businessInput.value = "";
    }

    if (roleSelect) {
        roleSelect.addEventListener("change", updateBusinessField);
        updateBusinessField();
    }

    const signupForm = document.getElementById("signupForm");
    const signupButton = document.getElementById("signupButton");

    if (signupForm && signupButton) {
        signupForm.addEventListener("submit", function (event) {
            const password = document.getElementById("password");
            const confirmPassword = document.getElementById("confirmPassword");

            if (password.value !== confirmPassword.value) {
                event.preventDefault();
                confirmPassword.setCustomValidity("Passwords do not match.");
                confirmPassword.reportValidity();
                return;
            }

            confirmPassword.setCustomValidity("");
            signupButton.disabled = true;
            signupButton.textContent = "Creating account…";
        });

        const confirmPassword = document.getElementById("confirmPassword");
        confirmPassword.addEventListener("input", function () {
            confirmPassword.setCustomValidity("");
        });
    }
});