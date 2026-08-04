import request from '@/config/axios'
import type { Dayjs } from 'dayjs';

/** 歌曲信息 */
export interface IndexSong {
          id: number; // 自增主键
          orgId?: string; // 外部歌曲业务ID
          name?: string; // 歌曲名称
          singerIds: string; // 歌手业务ID，第一期单值
          singerNames?: string; // 歌手名称，第一期单值
          albumNames: string; // 专辑名称，第一期单值
          hot?: number; // 歌曲热度
          isCopyright?: number; // 版权状态：1有版权，0无版权
          invalidate: string | Dayjs; // 版权到期日
          isEnabled?: number; // 搜索可用状态：1可搜索，0不可搜索
          firstStartState?: number; // 首次发布状态：1有效，0无效
          releaseDate: string | Dayjs; // 发行日期
          updater: string; // 更新者
          remark: string; // 备注
  }

// 歌曲 API
export const SearchSongApi = {
  // 查询歌曲分页
  listSearchSong: async (data: any) => {
    return await request.post({ url: `/api/search/song`, data })
  }
}
