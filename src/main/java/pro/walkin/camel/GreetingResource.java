package pro.walkin.camel;

//@Path("/hello")
public class GreetingResource {

    //    @GET
    public String hello() {
        return "Hello from Quarkus REST";
    }
}
