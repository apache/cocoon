<?xml version="1.0"?>

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:import href="copyover.xsl"/>

  <xsl:template match="faqs">
   <faqs title="{ @title}">
    <xsl:apply-templates />
   
    <faq>
 <question>
  How can I add my FAQ to this document? 
 </question>
 <answer>
  <p>
   Follow the instructions found in <link href="../howto/howto-author-faq.html">How-To Author an FAQ.</link> 
  </p>
 </answer>
</faq>

    <faq>
 <question>
  How can I suggest improvements to existing FAQs?
 </question>
 <answer>
  <p>
  Given the rapid pace of change with Cocoon, many individual FAQs quickly become out-of-date and confusing to new users. If you have the relevant knowledge, please consider updating other FAQs on this page for technical errors. If you see a few typos, please consider fixing them too.  Follow the instructions found in <link href="../howto/howto-author-faq.html">How-To Author an FAQ.</link> 
  </p>
 </answer>
</faq>

</faqs>
        
  </xsl:template>


</xsl:stylesheet>