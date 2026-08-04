<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="歌曲业务ID" prop="orgId">
        <el-input v-model="formData.orgId" placeholder="歌曲业务ID" />
      </el-form-item>
      <el-form-item label="歌曲名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入歌曲名称" />
      </el-form-item>
      <el-form-item label="歌手业务ID" prop="singerIds">
        <el-input v-model="formData.singerIds" placeholder="请输入歌手业务ID" />
      </el-form-item>
      <el-form-item label="歌手名称" prop="singerNames">
        <el-input v-model="formData.singerNames" placeholder="请输入歌手名称" />
      </el-form-item>
      <el-form-item label="专辑名称" prop="albumNames">
        <el-input v-model="formData.albumNames" placeholder="请输入专辑名称" />
      </el-form-item>
      <el-form-item label="歌曲热度" prop="hot">
        <el-input v-model="formData.hot" placeholder="请输入歌曲热度" />
      </el-form-item>
      <el-form-item label="版权状态：1有版权，0无版权" prop="isCopyright">
        <el-input v-model="formData.isCopyright" placeholder="请输入版权状态：1有版权，0无版权" />
      </el-form-item>
      <el-form-item label="版权到期日" prop="invalidate">
        <el-date-picker
          v-model="formData.invalidate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择版权到期日"
        />
      </el-form-item>
      <el-form-item label="搜索可用状态" prop="isEnabled">
        <el-input
          v-model="formData.isEnabled"
          placeholder="请输入搜索可用状态：1可搜索，0不可搜索"
        />
      </el-form-item>
      <el-form-item label="首次发布状态" prop="firstStartState">
        <el-input
          v-model="formData.firstStartState"
          placeholder="请输入首次发布状态：1有效，0无效"
        />
      </el-form-item>
      <el-form-item label="发行日期" prop="releaseDate">
        <el-date-picker
          v-model="formData.releaseDate"
          type="date"
          value-format="YYYY-MM-DD"
          placeholder="选择发行日期"
        />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { IndexSongApi, IndexSong } from '@/api/search/indexsong'

/** 歌曲 表单 */
defineOptions({ name: 'IndexSongForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗的是否展示
const dialogTitle = ref('') // 弹窗的标题
const formLoading = ref(false) // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formType = ref('') // 表单的类型：create - 新增；update - 修改
const formData = ref({
  id: undefined,
  orgId: undefined,
  name: undefined,
  singerIds: undefined,
  singerNames: undefined,
  albumNames: undefined,
  hot: undefined,
  isCopyright: undefined,
  invalidate: undefined,
  isEnabled: undefined,
  firstStartState: undefined,
  releaseDate: undefined,
  updater: undefined,
  remark: undefined
})
const formRules = reactive({
  name: [{ required: true, message: '歌曲名称不能为空', trigger: 'blur' }],
  singerNames: [{ required: true, message: '歌手名称不能为空', trigger: 'blur' }],
  hot: [{ required: true, message: '歌曲热度不能为空', trigger: 'blur' }]
})
const formRef = ref() // 表单 Ref

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  // 修改时，设置数据
  if (id) {
    formLoading.value = true
    try {
      formData.value = await IndexSongApi.getIndexSong(id)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // 提供 open 方法，用于打开弹窗

/** 提交表单 */
const emit = defineEmits(['success']) // 定义 success 事件，用于操作成功后的回调
const submitForm = async () => {
  // 校验表单
  await formRef.value.validate()
  // 提交请求
  formLoading.value = true
  try {
    const data = formData.value as unknown as IndexSong
    if (formType.value === 'create') {
      await IndexSongApi.createIndexSong(data)
      message.success(t('common.createSuccess'))
    } else {
      await IndexSongApi.updateIndexSong(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    // 发送操作成功的事件
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    orgId: undefined,
    name: undefined,
    singerIds: undefined,
    singerNames: undefined,
    albumNames: undefined,
    hot: undefined,
    isCopyright: undefined,
    invalidate: undefined,
    isEnabled: undefined,
    firstStartState: undefined,
    releaseDate: undefined,
    updater: undefined,
    remark: undefined
  }
  formRef.value?.resetFields()
}
</script>
