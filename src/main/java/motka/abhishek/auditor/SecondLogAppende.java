package motka.abhishek.auditor;

import motka.abhishek.auditor.interfaces.LogAppender;
import org.springframework.stereotype.Component;

@Component
public class SecondLogAppende implements LogAppender
{
    @Override
    public String appendToLog()
    {
        return "Second";
    }
}
