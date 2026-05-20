package healthajo.jdbc.core;

public class JoinOnStep {

    private final SelectStep selectStep;
    private final JoinClause.Type joinType;
    private final FromSource joinSource;

    JoinOnStep(SelectStep selectStep, JoinClause.Type joinType, FromSource joinSource) {
        this.selectStep = selectStep;
        this.joinType = joinType;
        this.joinSource = joinSource;
    }

    public SelectStep on(Condition condition) {
        selectStep.addJoin(new JoinClause(joinType, joinSource, condition));
        return selectStep;
    }
}
