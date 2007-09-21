package org.apache.cocoon.maven.javadocs.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.AbstractMavenReportRenderer;

/**
 * This reports creates a script that downloads the javadocs from a Maven
 * repository.
 *
 * @goal javadocs-script
 */
public class JavadocsScriptGeneratorReport extends AbstractMavenReport {

    /**
     * Report output directory.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     */
    private String outputDirectory;

    /**
     * Doxia Site Renderer.
     *
     * @component
     */
    private Renderer siteRenderer;

    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    public String getName(Locale locale) {
        return "JavaDocs";
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getCategoryName()
     */
    public String getCategoryName() {
        return CATEGORY_PROJECT_REPORTS;
    }

    /**
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    public String getDescription(Locale locale) {
        return "JavaDoc API documentation.";
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory() {
        return outputDirectory + "/apidocs";
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject() {
        return project;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#getSiteRenderer()
     */
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#executeReport(java.util.Locale)
     */
    public void executeReport(Locale locale) {
        WarningRenderer r = new WarningRenderer(project.getName(), getDescription(locale), getSink());
        r.render();
        FileOutputStream fos = null;
        try {
            File f = new File(getOutputDirectory(), "create-apidocs.sh");
            fos = new FileOutputStream(f);
            IOUtils.write(createScript(), fos);
        } catch (IOException e) {
        } finally {
            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }
        }
    }

    private String createScript() {
        StringBuffer sb = new StringBuffer();
        sb.append("tbd");
        return sb.toString();
    }

    public String getOutputName() {
        return "apidocs/index";
    }

    private static class WarningRenderer extends AbstractMavenReportRenderer {
        private final String title;

        private final String description;

        WarningRenderer(String title, String description, Sink sink) {
            super(sink);
            this.title = title;
            this.description = description;
        }

        /**
         * @see org.apache.maven.reporting.MavenReportRenderer#getTitle()
         */
        public String getTitle() {
            return title;
        }

        /**
         * @see org.apache.maven.reporting.AbstractMavenReportRenderer#renderBody()
         */
        public void renderBody() {
            startSection(description);
            paragraph("Run the download script in order to get the Java API docs instead of this page.");
            endSection();
        }
    }

}
