package serverPart.commandsClasses;


import dto.Message;
import dto.Route;
import serverPart.Logger;
import serverPart.interfaces.CommandManualNoParameters;

import java.util.*;
import java.util.stream.Collectors;

import static serverPart.Manager.routes;

/**
 * Класс команды show
 */
public class ShowCommand implements CommandManualNoParameters {
    private static final org.slf4j.Logger logger = Logger.getLogger("ShowCommand");
    /**
     * Метод execute команды show
     *
     * @return Message[]
     */
    @Override
    public List<Message> executeManual() {
        if (routes.size() != 0) {
            List<Message> arrayOfMessages = new ArrayList<>();
            arrayOfMessages.add(new Message(1, routes.size() + 1,
                    "Collection elements:"));
            List<Route> sortedRoute = routes.stream().sorted(Comparator.comparing(Route::getSize)).collect(Collectors.toList());
            sortedRoute.forEach(route -> arrayOfMessages.add(
                    new Message(arrayOfMessages.size(), sortedRoute.size() + 1, route)));
            logger.info("Command completed");
            return arrayOfMessages;
        } else {
            logger.info("Command completed: The collection has no elements");
            return new ArrayList<>(Collections.singleton(new Message(1, 1,
                    "The collection has no elements")));
        }

    }
}
