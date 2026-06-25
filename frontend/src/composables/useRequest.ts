import { ref, type Ref } from 'vue'

interface UseRequestOptions<T> {
  immediate?: boolean
  onSuccess?: (data: T) => void
  onError?: (error: Error) => void
}

export function useRequest<T>(
  fn: (...args: any[]) => Promise<T>,
  options: UseRequestOptions<T> = {},
) {
  const { immediate = false, onSuccess, onError } = options

  const loading = ref(false)
  const data = ref<T | null>(null) as Ref<T | null>
  const error = ref<Error | null>(null)

  const execute = async (...args: any[]) => {
    loading.value = true
    error.value = null
    try {
      const result = await fn(...args)
      data.value = result
      onSuccess?.(result)
      return result
    } catch (e) {
      const err = e as Error
      error.value = err
      onError?.(err)
      throw err
    } finally {
      loading.value = false
    }
  }

  if (immediate) {
    execute()
  }

  return { loading, data, error, execute }
}
