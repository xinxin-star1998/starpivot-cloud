-- 调用目标改为短写法 CleanOperLogTask.cleanOperLog()（清空全部操作日志）
UPDATE sys_job
SET invoke_target   = 'CleanOperLogTask.cleanOperLog()',
    cron_expression = '0 0 2 * * ?',
    remark          = '每天凌晨2点清空操作日志表 sys_oper_log',
    update_time     = NOW()
WHERE job_id = 1
   OR invoke_target LIKE '%CleanOperLogTask%';
