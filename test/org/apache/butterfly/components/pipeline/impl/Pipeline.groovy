import org.apache.butterfly.xml.dom.DOMBuilder
import org.apache.butterfly.components.pipeline.impl.NonCachingProcessingPipeline

class Pipeline {
    public beanFactory;
    private pipeline;
    
    protected Pipeline() {
        this.pipeline = new NonCachingProcessingPipeline()
    }
            
    protected void generate(src) {
        generator = beanFactory.getBean("fileGenerator")
        generator.inputSource = src
        this.pipeline.generator = generator
    }
    
    protected void transform(type, src) {
        factory = beanFactory.getBean(type + "TransformerFactory")
        transformer = factory.getTransformer(src)
        this.pipeline.addTransformer(transformer)
    }
    
    protected void serialize(type) {
        serializer = beanFactory.getBean(type + "Serializer")
        this.pipeline.serializer = serializer
    }
    
    public void process() {
        builder = new DOMBuilder()
        this.pipeline.process(null, builder)
        println(builder.document.documentElement)
    }
}