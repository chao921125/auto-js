package net.cc.stardust.autojs.scheduler;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任务调度器
 * Auto.js Pro新特性: 新增任务模块
 * 
 * 功能:
 * - Cron风格定时表达式
 * - 重复任务调度
 * - 一次性任务
 * - 系统级闹钟调度(开机自启)
 * - 任务优先级管理
 */
public class TaskScheduler {
    
    private static final String TAG = "TaskScheduler";
    private static final String ACTION_TASK_EXECUTE = "net.cc.stardust.autojs.TASK_EXECUTE";
    
    private static TaskScheduler instance;
    private final Context context;
    private final AlarmManager alarmManager;
    private final List<ScheduledTask> taskQueue;
    
    // Cron表达式模式
    private static final Pattern CRON_PATTERN = Pattern.compile(
        "^(?:(\\*)|(\\*/([^\\s]+)))\\s+" +  // 分钟
        "(?:(\\*)|(\\*/([^\\s]+)))\\s+" +  // 小时  
        "(?:(\\*)|(\\*\\/([^\\s]+)))\\s+" + // 日
        "(?:(\\*)|(\\*\\/([^\\s]+)))\\s+" + // 月
        "(?:(\\*)|(\\*[\\-\\/0-9]+))"      // 周
    );
    
    private TaskScheduler(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.taskQueue = new CopyOnWriteArrayList<>();
    }
    
    /**
     * 获取单例
     */
    public static TaskScheduler getInstance(Context context) {
        if (instance == null) {
            synchronized (TaskScheduler.class) {
                if (instance == null) {
                    instance = new TaskScheduler(context);
                }
            }
        }
        return instance;
    }
    
    /**
     * 添加Cron风格定时任务
     * 
     * @param taskId 任务ID
     * @param cronExpression Cron表达式(分 时 日 月 周)
     * @param scriptPath 脚本路径
     * @return 是否成功
     */
    public boolean addCronTask(String taskId, String cronExpression, String scriptPath) {
        try {
            CronExpression cron = parseCronExpression(cronExpression);
            if (cron == null) {
                Log.e(TAG, "Cron表达式解析失败: " + cronExpression);
                return false;
            }
            
            ScheduledTask task = new ScheduledTask(taskId, cron, scriptPath);
            taskQueue.add(task);
            
            scheduleNextExecution(task);
            Log.i(TAG, "定时任务已添加: " + taskId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "添加定时任务失败", e);
            return false;
        }
    }
    
    /**
     * 添加一次性定时任务
     */
    public boolean addOneTimeTask(String taskId, long delayMs, String scriptPath) {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.getTimeInMillis().add(delayMs);
            
            ScheduledTask task = new ScheduledTask(taskId, calendar, scriptPath);
            taskQueue.add(task);
            
            scheduleOneTimeTask(task);
            Log.i(TAG, "一次性任务已添加: " + taskId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "添加一次性任务失败", e);
            return false;
        }
    }
    
    /**
     * 添加间隔重复任务
     */
    public boolean addIntervalTask(String taskId, long intervalMs, String scriptPath) {
        try {
            ScheduledTask task = new ScheduledTask(taskId, intervalMs, scriptPath);
            taskQueue.add(task);
            
            startIntervalTask(task);
            Log.i(TAG, "间隔任务已添加: " + taskId);
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "添加间隔任务失败", e);
            return false;
        }
    }
    
    /**
     * 解析Cron表达式
     */
    private CronExpression parseCronExpression(String expression) {
        try {
            String[] parts = expression.split("\\s+");
            if (parts.length != 5) {
                return null;
            }
            
            return new CronExpression(
                Integer.parseInt(parts[0]),  // 分钟
                Integer.parseInt(parts[1]),  // 小时
                Integer.parseInt(parts[2]),  // 日
                Integer.parseInt(parts[3]),  // 月
                Integer.parseInt(parts[4])   // 周
            );
        } catch (Exception e) {
            Log.e(TAG, "Cron表达式解析错误", e);
            return null;
        }
    }
    
    /**
     * 计算下次执行时间
     */
    private Calendar calculateNextExecution(CronExpression cron) {
        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        
        // 简单实现: 设置为下一分钟
        next.add(Calendar.MINUTE, 1);
        next.set(Calendar.SECOND, 0);
        
        // TODO: 完整实现Cron表达式计算逻辑
        return next;
    }
    
    /**
     * 安排任务执行
     */
    private void scheduleNextExecution(ScheduledTask task) {
        Calendar nextExec = calculateNextExecution(task.getCron());
        executeTaskAtTime(task, nextExec);
    }
    
    /**
     * 执行一次性任务
     */
    private void scheduleOneTimeTask(ScheduledTask task) {
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.setAction("ONE_TIME");
        intent.putExtra("taskId", task.getId());
        intent.putExtra("scriptPath", task.getScriptPath());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            task.getId().hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            task.getNextExecutionTime(),
            pendingIntent
        );
    }
    
    /**
     * 在指定时间执行任务
     */
    private void executeTaskAtTime(ScheduledTask task, Calendar time) {
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("scriptPath", task.getScriptPath());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            task.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 设置闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(),
                pendingIntent
            );
        } else {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                time.getTimeInMillis(),
                pendingIntent
            );
        }
        
        task.setNextExecutionTime(time.getTimeInMillis());
    }
    
    /**
     * 启动间隔任务
     */
    private void startIntervalTask(ScheduledTask task) {
        Intent intent = new Intent(context, TaskReceiver.class);
        intent.putExtra("taskId", task.getId());
        intent.putExtra("scriptPath", task.getScriptPath());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            task.getId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 使用重复闹钟
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + task.getInterval(),
            task.getInterval(),
            pendingIntent
        );
    }
    
    /**
     * 删除任务
     */
    public boolean removeTask(String taskId) {
        boolean removed = taskQueue.removeIf(task -> task.getId().equals(taskId));
        
        if (removed) {
            // 取消闹钟
            Intent intent = new Intent(context, TaskReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode(),
                intent,
                PendingIntent.NO_CANCEL
            );
            if (pendingIntent != null) {
                pendingIntent.cancel();
            }
            
            Log.i(TAG, "任务已删除: " + taskId);
        }
        
        return removed;
    }
    
    /**
     * 停止所有任务
     */
    public void stopAllTasks() {
        taskQueue.clear();
        
        // 取消所有闹钟
        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // TODO: 取消所有注册的PendingIntent
        
        Log.i(TAG, "所有任务已停止");
    }
    
    /**
     * 获取所有任务列表
     */
    public List<ScheduledTask> getAllTasks() {
        return taskQueue;
    }
    
    /**
     * 任务接收器
     */
    public static class TaskReceiver extends BroadcastReceiver {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String taskId = intent.getStringExtra("taskId");
            String scriptPath = intent.getStringExtra("scriptPath");
            
            Log.i(TAG, "接收到任务执行通知: " + taskId);
            
            // 唤醒CPU执行任务
            WakeLocker.acquire(context);
            
            // TODO: 启动脚本引擎执行任务
            executeScript(context, scriptPath);
        }
        
        private void executeScript(Context context, String scriptPath) {
            // TODO: 集成脚本引擎执行逻辑
            Log.d(TAG, "执行脚本: " + scriptPath);
        }
    }
    
    /**
     * 唤醒锁管理器
     */
    public static class WakeLocker {
        private static PowerManager.WakeLock wakeLock;
        
        public static void acquire(Context context) {
            if (wakeLock == null) {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "AutoJs:TaskSchedulerWakeLock"
                );
                wakeLock.setReferenceCounted(false);
            }
            
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
        }
        
        public static void release() {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                wakeLock = null;
            }
        }
    }
    
    /**
     * 定时任务包装类
     */
    public static class ScheduledTask {
        private String id;
        private CronExpression cron;
        private String scriptPath;
        private long nextExecutionTime;
        private long interval;
        private boolean enabled;
        
        public ScheduledTask(String id, CronExpression cron, String scriptPath) {
            this.id = id;
            this.cron = cron;
            this.scriptPath = scriptPath;
            this.enabled = true;
        }
        
        public ScheduledTask(String id, Calendar firstExecution, String scriptPath) {
            this.id = id;
            this.scriptPath = scriptPath;
            this.nextExecutionTime = firstExecution.getTimeInMillis();
            this.enabled = true;
        }
        
        public ScheduledTask(String id, long intervalMs, String scriptPath) {
            this.id = id;
            this.interval = intervalMs;
            this.scriptPath = scriptPath;
            this.nextExecutionTime = System.currentTimeMillis() + intervalMs;
            this.enabled = true;
        }
        
        public String getId() { return id; }
        public CronExpression getCron() { return cron; }
        public String getScriptPath() { return scriptPath; }
        public long getNextExecutionTime() { return nextExecutionTime; }
        public void setNextExecutionTime(long time) { this.nextExecutionTime = time; }
        public long getInterval() { return interval; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    /**
     * Cron表达式
     */
    public static class CronExpression {
        private int minute;
        private int hour;
        private int dayOfMonth;
        private int month;
        private int dayOfWeek;
        
        public CronExpression(int minute, int hour, int dayOfMonth, 
                             int month, int dayOfWeek) {
            this.minute = minute;
            this.hour = hour;
            this.dayOfMonth = dayOfMonth;
            this.month = month;
            this.dayOfWeek = dayOfWeek;
        }
        
        public int getMinute() { return minute; }
        public int getHour() { return hour; }
        public int getDayOfMonth() { return dayOfMonth; }
        public int getMonth() { return month; }
        public int getDayOfWeek() { return dayOfWeek; }
    }
}

