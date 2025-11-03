// Switch to LPP tab when there are no LSP cards
window.addEventListener('load', function() {
    const lppTabLink = document.querySelector('#lpp-tab a');
    if (lppTabLink) {
        lppTabLink.click();
    }
});
