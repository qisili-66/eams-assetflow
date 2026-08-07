package org.example.eams.service;

import org.example.eams.dto.application.ApplicationAuditRequest;
import org.example.eams.dto.application.ApplicationCreateRequest;
import org.example.eams.dto.application.ApplicationQuery;
import org.example.eams.vo.ApplicationVo;
import org.example.eams.vo.PageResult;

public interface AssetApplicationService {

    Long create(ApplicationCreateRequest req, String username);

    PageResult<ApplicationVo> pageMy(ApplicationQuery query, String username);

    PageResult<ApplicationVo> page(ApplicationQuery query);

    ApplicationVo getById(Long id, String username, boolean isAdmin);

    void audit(Long id, ApplicationAuditRequest req, String username);

    void cancel(Long id, String username);
}
