document.addEventListener("DOMContentLoaded", function () {
    const lspTab = document.getElementById('lsp-tab');
    const lppTab = document.getElementById('lpp-tab');
    const lspTabLink = lspTab ? lspTab.querySelector('a.govuk-tabs__tab') : null;
    const lppTabLink = lppTab ? lppTab.querySelector('a.govuk-tabs__tab') : null;
    
    const lspPanel = document.getElementById('late-submission-penalties');
    const lppPanel = document.getElementById('late-payment-penalties');
    
    if (lspTab && lppTab && lspTabLink && lppTabLink && lspPanel && lppPanel) {
        lspTab.classList.remove('govuk-tabs__list-item--selected');
        lspTabLink.setAttribute('aria-selected', 'false');
        lspTabLink.removeAttribute('tabindex');
        
        lppTab.classList.add('govuk-tabs__list-item--selected');
        lppTabLink.setAttribute('aria-selected', 'true');
        lppTabLink.setAttribute('tabindex', '0');
        
        lspPanel.classList.add('govuk-tabs__panel--hidden');
        lppPanel.classList.remove('govuk-tabs__panel--hidden');
    }
})