<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="歌曲名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入歌曲名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column width="55" />
      <el-table-column label="外部歌曲业务ID" align="center" prop="orgId" />
      <el-table-column label="歌曲名称" align="center" prop="name" />
      <el-table-column label="歌手业务ID" align="center" prop="singerIds" />
      <el-table-column label="歌手名称" align="center" prop="singerNames" />
      <el-table-column label="专辑名称" align="center" prop="albumNames" />
      <el-table-column label="歌曲热度" align="center" prop="hot" />
      <el-table-column label="版权状态" align="center" prop="isCopyright" />
      <el-table-column label="版权到期日" align="center" prop="invalidate" />
    </el-table>
    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script setup lang="ts">
import { SearchSongApi, IndexSong } from '@/api/search/searchsong'

/** 歌曲 列表 */
defineOptions({ name: 'SearchSong' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const loading = ref(true) // 列表的加载中
const list = ref<IndexSong[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
})
const queryFormRef = ref() // 搜索的表单

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await SearchSongApi.listSearchSong(queryParams)
    list.value = data.c
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
