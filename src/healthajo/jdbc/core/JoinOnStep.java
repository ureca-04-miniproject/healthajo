package healthajo.jdbc.core;

public class JoinOnStep {

    private final SelectStep selectStep;
    private final JoinClause.Type joinType;
    private final String joinTable;

    JoinOnStep(SelectStep selectStep, JoinClause.Type joinType, String joinTable) {
        this.selectStep = selectStep;
        this.joinType = joinType;
        this.joinTable = joinTable;
    }

    public SelectStep on(Condition condition) {
        selectStep.addJoin(new JoinClause(joinType, joinTable, condition));
        return selectStep;
    }
}
