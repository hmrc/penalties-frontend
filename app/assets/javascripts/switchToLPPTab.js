document.addEventListener("DOMContentLoaded", function () {
    const tabs = document.querySelectorAll('[data-module="govuk-tabs"]');
    const tabsLib = new window.GOVUKFrontend.Tabs(tabs[0]);
    window.GOVUKFrontend.Tabs.prototype.unhighlightTab(tabsLib.$tabs[0]);
    window.GOVUKFrontend.Tabs.prototype.highlightTab(tabsLib.$tabs[1]);
    document.getElementById('late-payment-penalties').classList.remove('govuk-tabs__panel--hidden');
    document.getElementById('late-submission-penalties').classList.add('govuk-tabs__panel--hidden');
})