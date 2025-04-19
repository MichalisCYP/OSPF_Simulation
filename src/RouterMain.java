
import Controller.Controller;
import Model.Model;
import View.View;

public class RouterMain {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Model model = new Model("R" + port, port);
        View view = new View();
        Controller controller = new Controller(model, view);
        controller.startRouter();
    }
}
