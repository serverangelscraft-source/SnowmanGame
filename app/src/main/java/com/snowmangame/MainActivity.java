package com.snowmangame;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.rgb(239, 249, 255));
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
        if (Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
        }

        SnowmanView game = new SnowmanView(this);
        setContentView(game);
    }

    static class SnowmanView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Random rnd = new Random();
        private final ArrayList<Dust> dust = new ArrayList<>();

        private final float density;
        private final float scaledDensity;
        private final Vibrator vib;
        private final SharedPreferences prefs;

        private float safeTop = 0f;
        private float safeBottom = 0f;
        private float snow = 0f;
        private float lastX, lastY;
        private int balls = 0;
        private int score = 0;
        private int bestScore = 0;
        private boolean face = false;
        private boolean dressed = false;
        private boolean finished = false;
        private String tip = "Води пальцем по снігу";

        private final RectF makeBtn = new RectF();
        private final RectF faceBtn = new RectF();
        private final RectF dressBtn = new RectF();
        private final RectF finishBtn = new RectF();
        private final RectF collectRect = new RectF();
        private final RectF restartBtn = new RectF();

        SnowmanView(Context c) {
            super(c);
            density = getResources().getDisplayMetrics().density;
            scaledDensity = getResources().getDisplayMetrics().scaledDensity;
            vib = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
            prefs = c.getSharedPreferences("snowman_game", Context.MODE_PRIVATE);
            bestScore = prefs.getInt("best_score", 0);

            text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(dp(2));
            setFocusable(true);
            setClickable(true);
            setContentDescription("Гра Зліпи сніговика");

            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    if (Build.VERSION.SDK_INT >= 30) {
                        Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                        safeTop = bars.top;
                        safeBottom = bars.bottom;
                    } else {
                        safeTop = insets.getSystemWindowInsetTop();
                        safeBottom = insets.getSystemWindowInsetBottom();
                    }
                    invalidate();
                    return insets;
                }
            });
            requestApplyInsets();
        }

        private float dp(float value) { return value * density; }
        private float sp(float value) { return value * scaledDensity; }

        private void vibrate(int ms) {
            if (vib == null || !vib.hasVibrator()) return;
            if (Build.VERSION.SDK_INT >= 26) {
                vib.vibrate(VibrationEffect.createOneShot(ms, 90));
            } else {
                vib.vibrate(ms);
            }
        }

        @Override
        protected void onDraw(Canvas c) {
            super.onDraw(c);
            final float w = getWidth();
            final float h = getHeight();
            final float top = safeTop;
            final float bottom = h - safeBottom;
            final float margin = dp(14);

            drawBackground(c, w, h, top, bottom);

            float hudTop = top + dp(10);
            float hudH = dp(72);
            RectF hud = new RectF(margin, hudTop, w - margin, hudTop + hudH);
            drawHud(c, hud, w);

            float tipTop = hud.bottom + dp(8);
            RectF tipBox = new RectF(margin, tipTop, w - margin, tipTop + dp(38));
            drawTip(c, tipBox);

            float controlsTop = bottom - dp(82);
            float playTop = tipBox.bottom + dp(8);
            float playBottom = controlsTop - dp(10);
            float playH = Math.max(dp(180), playBottom - playTop);

            float collectH = Math.min(dp(96), playH * 0.30f);
            collectRect.set(margin, playBottom - collectH, w - margin, playBottom);
            drawCollector(c, collectRect);

            float snowRegionBottom = collectRect.top + dp(8);
            drawSnowman(c, w / 2f, playTop, snowRegionBottom);
            drawDust(c);
            drawControls(c, w, controlsTop, bottom);

            if (finished) drawFinishOverlay(c, w, top, bottom);
        }

        private void drawBackground(Canvas c, float w, float h, float top, float bottom) {
            LinearGradient sky = new LinearGradient(0, top, 0, bottom * 0.62f,
                    Color.rgb(172, 224, 250), Color.rgb(223, 244, 255), Shader.TileMode.CLAMP);
            p.setShader(sky);
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);

            p.setColor(Color.argb(210, 255, 255, 255));
            c.drawOval(new RectF(-w * .32f, bottom * .43f, w * .72f, bottom * .72f), p);
            p.setColor(Color.argb(235, 248, 253, 255));
            c.drawOval(new RectF(w * .28f, bottom * .48f, w * 1.28f, bottom * .76f), p);

            p.setColor(Color.rgb(242, 250, 255));
            c.drawRect(0, bottom * .61f, w, h, p);

            p.setColor(Color.argb(150, 255, 255, 255));
            int flakeCount = 26;
            int usable = Math.max(1, (int) (bottom - top - dp(90)));
            for (int i = 0; i < flakeCount; i++) {
                float x = (i * 137f + 29f) % Math.max(1f, w);
                float y = top + dp(24) + ((i * 191) % usable);
                float r = dp(1.2f + (i % 3) * .55f);
                c.drawCircle(x, y, r, p);
            }
        }

        private void drawHud(Canvas c, RectF r, float w) {
            p.setColor(Color.argb(238, 255, 255, 255));
            c.drawRoundRect(r, dp(22), dp(22), p);

            float x = r.left + dp(18);
            text.setTextAlign(Paint.Align.LEFT);
            text.setColor(Color.rgb(41, 71, 92));
            text.setTextSize(sp(11));
            c.drawText("СНІГ", x, r.top + dp(23), text);

            text.setTextSize(sp(18));
            c.drawText((int) snow + "%", x, r.top + dp(48), text);

            float barLeft = x + dp(58);
            float barRight = r.right - dp(94);
            float barTop = r.top + dp(33);
            float barH = dp(10);
            RectF bar = new RectF(barLeft, barTop, barRight, barTop + barH);
            p.setColor(Color.rgb(222, 238, 247));
            c.drawRoundRect(bar, barH / 2f, barH / 2f, p);
            float progress = Math.max(0f, Math.min(1f, snow / 100f));
            RectF fill = new RectF(bar.left, bar.top, bar.left + bar.width() * progress, bar.bottom);
            p.setColor(snow >= 100 ? Color.rgb(61, 151, 128) : Color.rgb(62, 139, 185));
            if (fill.width() > 0) c.drawRoundRect(fill, barH / 2f, barH / 2f, p);

            text.setTextAlign(Paint.Align.RIGHT);
            text.setColor(Color.rgb(31, 74, 104));
            text.setTextSize(sp(16));
            c.drawText("★ " + score, r.right - dp(17), r.top + dp(29), text);
            text.setTextSize(sp(9.5f));
            text.setColor(Color.rgb(104, 132, 149));
            c.drawText("РЕКОРД " + bestScore, r.right - dp(17), r.top + dp(50), text);
        }

        private void drawTip(Canvas c, RectF r) {
            p.setColor(Color.argb(205, 235, 248, 255));
            c.drawRoundRect(r, dp(18), dp(18), p);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(sp(12.5f));
            text.setColor(Color.rgb(37, 78, 105));
            c.drawText(tip, r.centerX(), r.centerY() + dp(4.5f), text);
        }

        private void drawCollector(Canvas c, RectF r) {
            p.setColor(Color.argb(190, 255, 255, 255));
            c.drawRoundRect(r, dp(24), dp(24), p);

            stroke.setColor(snow >= 100 ? Color.rgb(81, 159, 132) : Color.rgb(145, 197, 224));
            stroke.setStrokeWidth(dp(1.5f));
            c.drawRoundRect(new RectF(r.left + dp(1), r.top + dp(1), r.right - dp(1), r.bottom - dp(1)),
                    dp(23), dp(23), stroke);

            float previewX = r.left + dp(54);
            float previewY = r.centerY();
            float previewR = dp(13) + dp(17) * Math.max(0f, Math.min(1f, snow / 100f));
            RadialGradient g = new RadialGradient(previewX - previewR * .25f, previewY - previewR * .30f,
                    previewR * 1.35f,
                    new int[]{Color.WHITE, Color.rgb(236, 248, 255), Color.rgb(192, 224, 240)},
                    null, Shader.TileMode.CLAMP);
            p.setShader(g);
            c.drawCircle(previewX, previewY, previewR, p);
            p.setShader(null);

            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(sp(11.5f));
            text.setColor(Color.rgb(74, 133, 165));
            String line = snow >= 100 ? "КУЛЯ ГОТОВА" : "СВАЙПАЙ ПО СНІГУ";
            c.drawText(line, r.left + dp(96), r.centerY() - dp(2), text);
            text.setTextSize(sp(9.5f));
            text.setColor(Color.rgb(124, 160, 181));
            c.drawText("Збери 100% для наступної кулі", r.left + dp(96), r.centerY() + dp(17), text);
        }

        private void drawSnowman(Canvas c, float cx, float top, float bottom) {
            float availableH = Math.max(dp(120), bottom - top);
            float baseR = Math.min(getWidth() * .215f, availableH / 4.25f);
            baseR = Math.max(dp(38), baseR);
            float midR = baseR * .72f;
            float headR = baseR * .54f;

            float baseY = bottom - dp(5) - baseR;
            float midY = baseY - (baseR + midR) * .88f;
            float headY = midY - (midR + headR) * .88f;

            if (balls >= 1) ball(c, cx, baseY, baseR);
            if (balls >= 2) ball(c, cx, midY, midR);
            if (balls >= 3) ball(c, cx, headY, headR);

            if (balls == 0) {
                text.setTextAlign(Paint.Align.CENTER);
                text.setTextSize(sp(13));
                text.setColor(Color.rgb(92, 145, 173));
                c.drawText("ЗЛІПИ ПЕРШУ КУЛЮ", cx, top + availableH * .48f, text);
            }

            if (face && balls >= 3) {
                p.setColor(Color.rgb(37, 52, 64));
                c.drawCircle(cx - headR * .30f, headY - headR * .17f, headR * .075f, p);
                c.drawCircle(cx + headR * .30f, headY - headR * .17f, headR * .075f, p);

                Path nose = new Path();
                nose.moveTo(cx, headY + headR * .02f);
                nose.lineTo(cx + headR * .72f, headY + headR * .10f);
                nose.lineTo(cx, headY + headR * .18f);
                nose.close();
                p.setColor(Color.rgb(244, 118, 35));
                c.drawPath(nose, p);

                p.setColor(Color.rgb(49, 65, 76));
                for (int i = -2; i <= 2; i++) {
                    float mx = cx + i * headR * .16f;
                    float my = headY + headR * .42f + Math.abs(i) * headR * .035f;
                    c.drawCircle(mx, my, headR * .045f, p);
                }

                for (int i = 0; i < 3; i++) {
                    c.drawCircle(cx, midY - midR * .35f + i * midR * .39f, midR * .055f, p);
                }
            }

            if (dressed && balls >= 3) {
                // arms
                stroke.setColor(Color.rgb(105, 78, 57));
                stroke.setStrokeWidth(Math.max(dp(3), baseR * .035f));
                stroke.setStrokeCap(Paint.Cap.ROUND);
                c.drawLine(cx - midR * .72f, midY - midR * .10f, cx - midR * 1.55f, midY - midR * .55f, stroke);
                c.drawLine(cx + midR * .72f, midY - midR * .10f, cx + midR * 1.55f, midY - midR * .55f, stroke);
                c.drawLine(cx - midR * 1.45f, midY - midR * .49f, cx - midR * 1.70f, midY - midR * .75f, stroke);
                c.drawLine(cx + midR * 1.45f, midY - midR * .49f, cx + midR * 1.70f, midY - midR * .75f, stroke);
                stroke.setStrokeCap(Paint.Cap.BUTT);

                // hat
                p.setColor(Color.rgb(45, 62, 78));
                c.drawRoundRect(new RectF(cx - headR * .75f, headY - headR * 1.24f,
                        cx + headR * .75f, headY - headR * 1.05f), dp(5), dp(5), p);
                c.drawRoundRect(new RectF(cx - headR * .50f, headY - headR * 1.88f,
                        cx + headR * .50f, headY - headR * 1.12f), dp(8), dp(8), p);
                p.setColor(Color.rgb(71, 132, 164));
                c.drawRect(cx - headR * .50f, headY - headR * 1.28f,
                        cx + headR * .50f, headY - headR * 1.15f, p);

                // scarf
                p.setColor(Color.rgb(203, 65, 69));
                c.drawRoundRect(new RectF(cx - midR * .83f, midY - midR * .86f,
                        cx + midR * .83f, midY - midR * .64f), dp(8), dp(8), p);
                c.drawRoundRect(new RectF(cx + midR * .35f, midY - midR * .70f,
                        cx + midR * .60f, midY + midR * .10f), dp(6), dp(6), p);
            }
        }

        private void ball(Canvas c, float x, float y, float r) {
            RadialGradient g = new RadialGradient(x - r * .30f, y - r * .35f, r * 1.35f,
                    new int[]{Color.WHITE, Color.rgb(247, 252, 255), Color.rgb(207, 232, 245)},
                    new float[]{0f, .58f, 1f}, Shader.TileMode.CLAMP);
            p.setShader(g);
            c.drawCircle(x, y, r, p);
            p.setShader(null);

            stroke.setColor(Color.argb(90, 121, 176, 205));
            stroke.setStrokeWidth(dp(1));
            c.drawCircle(x, y, r - dp(.5f), stroke);
        }

        private void drawControls(Canvas c, float w, float top, float bottom) {
            float outer = dp(10);
            float gap = dp(6);
            float buttonTop = top + dp(7);
            float buttonBottom = Math.min(bottom - dp(7), buttonTop + dp(62));
            float bw = (w - outer * 2 - gap * 3) / 4f;

            makeBtn.set(outer, buttonTop, outer + bw, buttonBottom);
            faceBtn.set(makeBtn.right + gap, buttonTop, makeBtn.right + gap + bw, buttonBottom);
            dressBtn.set(faceBtn.right + gap, buttonTop, faceBtn.right + gap + bw, buttonBottom);
            finishBtn.set(dressBtn.right + gap, buttonTop, w - outer, buttonBottom);

            p.setColor(Color.argb(185, 249, 253, 255));
            c.drawRoundRect(new RectF(dp(4), top, w - dp(4), bottom), dp(24), dp(24), p);

            drawButton(c, makeBtn, "КУЛЯ", balls + "/3", balls < 3 && snow >= 100, balls >= 3);
            drawButton(c, faceBtn, "ОБЛИЧЧЯ", face ? "ГОТОВО" : "КРОК 2", balls >= 3 && !face, face);
            drawButton(c, dressBtn, "ОДЯГ", dressed ? "ГОТОВО" : "КРОК 3", face && !dressed, dressed);
            drawButton(c, finishBtn, "ФІНІШ", finished ? "ГОТОВО" : "КРОК 4", dressed && !finished, finished);
        }

        private void drawButton(Canvas c, RectF r, String title, String sub, boolean active, boolean done) {
            if (done) p.setColor(Color.rgb(87, 154, 132));
            else if (active) p.setColor(Color.rgb(38, 105, 145));
            else p.setColor(Color.rgb(215, 229, 237));
            c.drawRoundRect(r, dp(17), dp(17), p);

            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(sp(10.5f));
            text.setColor((active || done) ? Color.WHITE : Color.rgb(112, 137, 151));
            c.drawText(title, r.centerX(), r.centerY() - dp(2), text);
            text.setTextSize(sp(8));
            text.setColor((active || done) ? Color.argb(225, 255, 255, 255) : Color.rgb(151, 170, 181));
            c.drawText(sub, r.centerX(), r.centerY() + dp(15), text);
        }

        private void drawDust(Canvas c) {
            if (dust.isEmpty()) return;
            Iterator<Dust> it = dust.iterator();
            boolean needsFrame = false;
            while (it.hasNext()) {
                Dust d = it.next();
                d.alpha -= 13;
                d.radius += dp(.10f);
                if (d.alpha <= 0) {
                    it.remove();
                    continue;
                }
                needsFrame = true;
                p.setColor(Color.argb(d.alpha, 255, 255, 255));
                c.drawCircle(d.x, d.y, d.radius, p);
            }
            if (needsFrame) postInvalidateOnAnimation();
        }

        private void addDust(float x, float y, float distance) {
            if (dust.size() > 28) dust.remove(0);
            float r = dp(2.5f) + Math.min(dp(5), distance * .02f);
            dust.add(new Dust(x + rnd.nextFloat() * dp(10) - dp(5),
                    y + rnd.nextFloat() * dp(10) - dp(5), r, 210));
        }

        private void drawFinishOverlay(Canvas c, float w, float top, float bottom) {
            p.setColor(Color.argb(190, 22, 45, 61));
            c.drawRect(0, 0, getWidth(), getHeight(), p);

            float cardW = Math.min(w - dp(34), dp(360));
            float cardH = Math.min(bottom - top - dp(50), dp(330));
            float left = (w - cardW) / 2f;
            float cardTop = top + (bottom - top - cardH) / 2f;
            RectF card = new RectF(left, cardTop, left + cardW, cardTop + cardH);
            p.setColor(Color.WHITE);
            c.drawRoundRect(card, dp(28), dp(28), p);

            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.rgb(32, 71, 96));
            text.setTextSize(sp(20));
            c.drawText("Сніговик готовий!", card.centerX(), card.top + dp(54), text);

            text.setTextSize(sp(42));
            text.setColor(Color.rgb(40, 120, 161));
            c.drawText(String.valueOf(score), card.centerX(), card.top + dp(121), text);

            text.setTextSize(sp(10));
            text.setColor(Color.rgb(117, 144, 159));
            c.drawText("РЕКОРД: " + bestScore, card.centerX(), card.top + dp(147), text);

            restartBtn.set(card.left + dp(26), card.bottom - dp(78), card.right - dp(26), card.bottom - dp(24));
            p.setColor(Color.rgb(38, 105, 145));
            c.drawRoundRect(restartBtn, dp(18), dp(18), p);
            text.setTextSize(sp(13));
            text.setColor(Color.WHITE);
            c.drawText("ГРАТИ ЩЕ", restartBtn.centerX(), restartBtn.centerY() + dp(5), text);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = x;
                lastY = y;
                return true;
            }

            if (e.getAction() == MotionEvent.ACTION_MOVE) {
                if (!finished && balls < 3 && collectRect.contains(x, y)) {
                    float d = (float) Math.hypot(x - lastX, y - lastY);
                    if (d > dp(1.5f)) {
                        snow = Math.min(100f, snow + d / dp(7f));
                        addDust(x, y, d);
                        if (snow >= 100f) tip = "Куля готова — натисни «КУЛЯ»";
                        else tip = "Накочуй сніг: " + (int) snow + "%";
                        lastX = x;
                        lastY = y;
                        invalidate();
                    }
                } else {
                    lastX = x;
                    lastY = y;
                }
                return true;
            }

            if (e.getAction() == MotionEvent.ACTION_UP) {
                performClick();
                if (finished) {
                    if (restartBtn.contains(x, y)) reset();
                    return true;
                }
                if (makeBtn.contains(x, y)) makeBall();
                else if (faceBtn.contains(x, y)) addFace();
                else if (dressBtn.contains(x, y)) addDress();
                else if (finishBtn.contains(x, y)) finish();
                return true;
            }

            return true;
        }

        @Override
        public boolean performClick() {
            super.performClick();
            return true;
        }

        private void makeBall() {
            if (balls >= 3) return;
            if (snow < 100f) {
                tip = "Спочатку назбирай 100% снігу";
                vibrate(15);
                invalidate();
                return;
            }
            snow = 0f;
            balls++;
            score += 100;
            vibrate(32);
            tip = balls < 3 ? "Чудово. Збери сніг для кулі " + (balls + 1) : "Три кулі готові — додай обличчя";
            invalidate();
        }

        private void addFace() {
            if (balls < 3 || face) {
                if (balls < 3) tip = "Спочатку зроби три снігові кулі";
                invalidate();
                return;
            }
            face = true;
            score += 150;
            tip = "Є обличчя. Тепер додай одяг";
            vibrate(25);
            invalidate();
        }

        private void addDress() {
            if (!face || dressed) {
                if (!face) tip = "Спочатку додай обличчя";
                invalidate();
                return;
            }
            dressed = true;
            score += 150;
            tip = "Сніговик одягнений — можна завершувати";
            vibrate(25);
            invalidate();
        }

        private void finish() {
            if (!dressed || finished) {
                if (!dressed) tip = "Спочатку одягни сніговика";
                invalidate();
                return;
            }
            score += 100 + rnd.nextInt(101);
            finished = true;
            if (score > bestScore) {
                bestScore = score;
                prefs.edit().putInt("best_score", bestScore).apply();
            }
            vibrate(60);
            invalidate();
        }

        private void reset() {
            snow = 0f;
            balls = 0;
            score = 0;
            face = false;
            dressed = false;
            finished = false;
            tip = "Води пальцем по снігу";
            dust.clear();
            vibrate(20);
            invalidate();
        }

        static class Dust {
            float x, y, radius;
            int alpha;
            Dust(float x, float y, float radius, int alpha) {
                this.x = x;
                this.y = y;
                this.radius = radius;
                this.alpha = alpha;
            }
        }
    }
}
