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
export const IndexSongApi = {
  // 查询歌曲分页
  getIndexSongPage: async (params: any) => {
    return await request.get({ url: `/search/index-song/page`, params })
  },

  // 查询歌曲详情
  getIndexSong: async (id: number) => {
    return await request.get({ url: `/search/index-song/get?id=` + id })
  },

  // 新增歌曲
  createIndexSong: async (data: IndexSong) => {
    return await request.post({ url: `/search/index-song/create`, data })
  },

  // 修改歌曲
  updateIndexSong: async (data: IndexSong) => {
    return await request.put({ url: `/search/index-song/update`, data })
  },

  // 删除歌曲
  deleteIndexSong: async (id: number) => {
    return await request.delete({ url: `/search/index-song/delete?id=` + id })
  },

  /** 批量删除歌曲 */
  deleteIndexSongList: async (ids: number[]) => {
    return await request.delete({ url: `/search/index-song/delete-list?ids=${ids.join(',')}` })
  },

  // 导出歌曲 Excel
  exportIndexSong: async (params) => {
    return await request.download({ url: `/search/index-song/export-excel`, params })
  },
}
