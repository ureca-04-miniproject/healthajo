package healthajo.program.application;

import healthajo.jdbc.core.Page;
import healthajo.program.dao.ProgramDao;
import healthajo.program.domain.ProgramSummary;

public class ProgramApplication {

    private final ProgramDao dao = new ProgramDao();

    public Page<ProgramSummary> findAll(int pageNumber, int pageSize, String keyword) {
        return dao.findAll(pageNumber, pageSize, keyword);
    }
}
