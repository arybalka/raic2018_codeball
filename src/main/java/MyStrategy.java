import model.*;

public final class MyStrategy implements Strategy {
    @Override
    public void act(Robot me, Rules rules, Game game, Action action) {
		Agent.act(me, rules, game, action);
    }
    
    @Override
    public String customRendering() {
        return "";
    }
}
