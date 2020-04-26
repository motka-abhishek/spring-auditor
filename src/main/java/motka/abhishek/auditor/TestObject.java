package motka.abhishek.auditor;

import motka.abhishek.auditor.annotations.Audit;
import motka.abhishek.auditor.annotations.Mask;

@Audit
public class TestObject
{
    public @Mask("hello") Modal print(@Mask Modal name) {
        System.out.println("Test Name: " + name.world);
        return name;
    }

    public void printHello(@Mask String name) {
        throw new NullPointerException("name can't be null");
    }
}
