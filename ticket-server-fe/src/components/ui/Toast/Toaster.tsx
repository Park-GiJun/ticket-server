import { useToastStore } from '../../../store/toastStore';
import styles from './Toaster.module.css';

const ICONS: Record<string, string> = {
  error: '⚠️',
  success: '✅',
  info: 'ℹ️',
};

/** 전역 토스트 렌더러. main 에서 한 번만 마운트한다. */
export function Toaster() {
  const toasts = useToastStore((s) => s.toasts);
  const remove = useToastStore((s) => s.remove);

  if (toasts.length === 0) return null;

  return (
    <div className={styles.wrap} role="region" aria-label="알림">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`${styles.toast} ${styles[t.type]}`}
          role="status"
          onClick={() => remove(t.id)}
        >
          <span className={styles.icon} aria-hidden="true">
            {ICONS[t.type]}
          </span>
          <span className={styles.msg}>{t.message}</span>
        </div>
      ))}
    </div>
  );
}

export default Toaster;
