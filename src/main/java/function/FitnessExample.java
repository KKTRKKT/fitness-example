package function;

import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.*;

public class FitnessExample {
    public String testableHtml(PageData pageData, boolean includeSuiteSetup) throws Exception {
        return new TestableHtmlBuilder(pageData, includeSuiteSetup).surround();
    }

    private class TestableHtmlBuilder {
        private final StringBuffer buffer;
        private PageData pageData;
        private boolean includeSuiteSetup;
        private WikiPage wikiPage;

        public TestableHtmlBuilder(PageData pageData, boolean includeSuiteSetup) {
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
            wikiPage = pageData.getWikiPage();
            buffer = new StringBuffer();
        }

        public String surround() throws Exception {
            if (ifTestPage())
                surroundPageWithSetUpsAndTearDowns();
            return pageData.getHtml();
        }

        private void surroundPageWithSetUpsAndTearDowns() throws Exception {
            includeSetup();
            buffer.append(pageData.getContent());
            includeTeardowns();
            pageData.setContent(buffer.toString());
        }

        private boolean ifTestPage() throws Exception {
            return pageData.hasAttribute("Test");
        }

        private void includeTeardowns() throws Exception {
            includeInherited("TearDown", "teardown");
            if (includeSuiteSetup) {
                includeInherited(SuiteResponder.SUITE_TEARDOWN_NAME, "teardown");
            }
        }

        private void includeSetup() throws Exception {
            if (includeSuiteSetup) {
                includeInherited(SuiteResponder.SUITE_SETUP_NAME, "setup");
            }
            includeInherited("SetUp", "setup");
        }

        private void includeInherited(String pageName, String mode) throws Exception {
            WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(pageName, wikiPage);
            if (suiteTeardown != null) {
                includePage(suiteTeardown, mode);
            }
        }

        private void includePage(WikiPage suiteTeardown, String mode) throws Exception {
            WikiPagePath pagePath = wikiPage.getPageCrawler().getFullPath(suiteTeardown);
            String pagePathName = PathParser.render(pagePath);
            buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
        }
    }
}
