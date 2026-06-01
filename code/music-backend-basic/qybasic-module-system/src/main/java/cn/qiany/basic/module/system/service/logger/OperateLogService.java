package cn.qiany.basic.module.system.service.logger;

import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import cn.qiany.basic.module.system.api.logger.dto.OperateLogPageReqDTO;
import cn.qiany.basic.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.qiany.basic.module.system.dal.dataobject.logger.OperateLogDO;

/**
 * 操作日志 Service 接口
 *
 * @author 芋道源码
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 创建请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志
     *
     * @param id 编号
     * @return 操作日志
     */
    OperateLogDO getOperateLog(Long id);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqDTO pageReqVO);

}
