package function;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.*;

public class FitnessExample {
    public String testableHtml(PageData pageData, boolean includeSuiteSetup) throws Exception {
        return new TestableHtml(pageData, includeSuiteSetup).surround();
    }

    private class TestableHtml {
        private PageData pageData;
        private boolean includeSuiteSetup;
        private WikiPage wikiPage;
        private StringBuffer buffer;

        public TestableHtml(PageData pageData, boolean includeSuiteSetup) {
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
            wikiPage = pageData.getWikiPage();
            buffer = new StringBuffer();
        }

        public String surround() throws Exception {
            if(isTestPage())
                includeSetupsAndTeardowns();
            pageData.setContent(buffer.toString());
            return pageData.getHtml();
        }

        private boolean isTestPage() throws Exception {
            return pageData.hasAttribute("Test");
        }

        private void includeSetupsAndTeardowns() throws Exception {
            includeSetups();
            buffer.append(pageData.getContent());
            includeTeardowns();
        }

        private void includeTeardowns() throws Exception {
            includeInherited("teardown", "TearDown");
            if (includeSuiteSetup)
                includeInherited("teardown", SuiteResponder.SUITE_TEARDOWN_NAME);
        }

        private void includeSetups() throws Exception {
            if (includeSuiteSetup)
                includeInherited("setup", SuiteResponder.SUITE_SETUP_NAME);
            includeInherited("setup", "SetUp");
        }

        private void includeInherited(String mode, String suiteTeardownName) throws Exception {
            WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(suiteTeardownName, wikiPage);
            if (suiteTeardown != null)
                includePage(suiteTeardown, mode);
        }

        private void includePage(WikiPage suiteTeardown, String mode) throws Exception {
            WikiPagePath pagePath = wikiPage.getPageCrawler().getFullPath(suiteTeardown);
            String pagePathName = PathParser.render(pagePath);
            buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
        }
    }
}