import com.intuit.karate.junit5.Karate;

public class KarateTests {

    @Karate.Test
    Karate testAccount() {
        return Karate.run("karate/account").relativeTo(getClass());
    }

}
