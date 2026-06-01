package cn.qiany.basic.module.infra.dal.mysql.file;

import cn.qiany.basic.framework.common.pojo.PageResult;
import cn.qiany.basic.framework.mybatis.core.mapper.BaseMapperX;
import cn.qiany.basic.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.qiany.basic.module.infra.controller.admin.file.vo.file.FilePageReqVO;
import cn.qiany.basic.module.infra.dal.dataobject.file.FileDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件操作 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FileMapper extends BaseMapperX<FileDO> {

    default PageResult<FileDO> selectPage(FilePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileDO>()
                .likeIfPresent(FileDO::getPath, reqVO.getPath())
                .likeIfPresent(FileDO::getType, reqVO.getType())
                .betweenIfPresent(FileDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileDO::getId));
    }

}
