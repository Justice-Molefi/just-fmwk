

public class Main {

    @Inject
    private static OrderService orderService;
    public static void main(String[] args){
        JustFmwk.setupApplication(Main.class);
        orderService.userService.createUser();
    }
}


@Component
class OrderService {
    @Inject
    public UserService userService;
}

@Component
class UserService {
    public void createUser() {
        System.out.println("Creating user");
    }
}