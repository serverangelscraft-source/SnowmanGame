package com.snowmangame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

public class IntroActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            w.setStatusBarColor(Color.rgb(177, 226, 248));
            w.setNavigationBarColor(Color.rgb(238, 249, 255));
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if (Build.VERSION.SDK_INT >= 29) w.setNavigationBarContrastEnforced(false);
        setContentView(new StoryView());
    }

    final class StoryView extends View {
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF skipRect = new RectF();
        final RectF startRect = new RectF();
        final float density;
        final float textScale;
        final long storyStart = SystemClock.elapsedRealtime();
        boolean leaving = false;

        StoryView() {
            super(IntroActivity.this);
            density = getResources().getDisplayMetrics().density;
            textScale = Math.min(getResources().getDisplayMetrics().scaledDensity, density * 1.16f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);
        }

        float dp(float v) { return v * density; }
        float tx(float v) { return v * textScale; }
        float clamp(float v, float a, float b) { return Math.max(a, Math.min(b, v)); }
        float smooth(float v) {
            v = clamp(v, 0f, 1f);
            return v * v * (3f - 2f * v);
        }
        float mix(float a, float b, float t) { return a + (b - a) * t; }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w = getWidth(), h = getHeight();
            float t = (SystemClock.elapsedRealtime() - storyStart) / 1000f;

            drawWinter(c, w, h, t);
            drawFriends(c, w, h, t);
            drawStoryText(c, w, h, t);
            drawSkip(c, w);
            if (t >= 7.0f) drawStart(c, w, h, t);

            if (t >= 10.5f) startGame();
            if (!leaving) postInvalidateOnAnimation();
        }

        void drawWinter(Canvas c, float w, float h, float t) {
            LinearGradient sky = new LinearGradient(0, 0, 0, h * .78f,
                    Color.rgb(166, 222, 249), Color.rgb(226, 246, 255), Shader.TileMode.CLAMP);
            p.setShader(sky);
            c.drawRect(0, 0, w, h, p);
            p.setShader(null);

            p.setColor(Color.argb(235, 255, 255, 255));
            c.drawOval(new RectF(-w * .35f, h * .55f, w * .72f, h * .82f), p);
            p.setColor(Color.rgb(244, 251, 255));
            c.drawOval(new RectF(w * .24f, h * .58f, w * 1.28f, h * .84f), p);
            p.setColor(Color.rgb(239, 249, 254));
            c.drawRect(0, h * .70f, w, h, p);

            // Distant trees.
            p.setColor(Color.argb(52, 56, 112, 133));
            for (int i = 0; i < 6; i++) {
                float x = w * (.04f + i * .19f);
                float y = h * (.69f + (i % 2) * .012f);
                float s = dp(14 + (i % 3) * 4);
                Path tr = new Path();
                tr.moveTo(x, y - s * 2.3f);
                tr.lineTo(x - s, y);
                tr.lineTo(x + s, y);
                tr.close();
                c.drawPath(tr, p);
            }

            // Continuous snowfall behind the main characters.
            for (int i = 0; i < 42; i++) {
                float speed = dp(20 + (i % 7) * 3);
                float yy = (i * dp(41) + t * speed) % (h * .78f);
                float xx = (i * dp(73) + dp(18) + (float)Math.sin(t * .7f + i) * dp(8)) % Math.max(1f, w);
                float rr = dp(.9f + (i % 4) * .38f);
                p.setColor(Color.argb(115 + (i % 3) * 35, 255, 255, 255));
                c.drawCircle(xx, yy, rr, p);
            }
        }

        void drawFriends(Canvas c, float w, float h, float t) {
            final int count = 10;
            float cx = w / 2f;
            float groundY = h * .62f;
            float gather = smooth((t - 2.2f) / 2.0f);
            float hug = smooth((t - 4.45f) / 2.15f);
            float becomeBall = smooth((t - 6.55f) / 1.15f);

            float[] xs = new float[count];
            float[] ys = new float[count];
            float ringR = Math.min(w * .25f, dp(92));
            float tightR = Math.min(w * .105f, dp(40));

            for (int i = 0; i < count; i++) {
                float startX = w * (.09f + ((i * 37) % 83) / 100f);
                float startY = h * (.16f + ((i * 29) % 35) / 100f);
                startY += Math.min(t, 2.5f) * dp(23 + (i % 4) * 2);

                double a = -Math.PI / 2 + i * Math.PI * 2 / count;
                float ringX = cx + (float)Math.cos(a) * ringR;
                float ringY = groundY + (float)Math.sin(a) * ringR * .56f;
                float tightX = cx + (float)Math.cos(a) * tightR;
                float tightY = groundY + (float)Math.sin(a) * tightR * .72f;

                float x = mix(startX, ringX, gather);
                float y = mix(startY, ringY, gather);
                x = mix(x, tightX, hug);
                y = mix(y, tightY, hug);
                xs[i] = x;
                ys[i] = y;
            }

            // When the friends decide to unite, their arms visibly connect.
            if (t >= 4.35f && becomeBall < .92f) {
                int alpha = (int)(210 * (1f - becomeBall));
                stroke.setColor(Color.argb(alpha, 91, 151, 181));
                stroke.setStrokeWidth(dp(2.2f));
                for (int i = 0; i < count; i++) {
                    int j = (i + 1) % count;
                    c.drawLine(xs[i], ys[i], xs[j], ys[j], stroke);
                }
            }

            int friendAlpha = (int)(255 * (1f - becomeBall));
            if (friendAlpha > 8) {
                for (int i = 0; i < count; i++) {
                    float r = dp(13 + (i % 3));
                    drawFriendFlake(c, xs[i], ys[i], r, friendAlpha, i);
                }
            }

            // The joined friends compress into one soft snowball.
            if (becomeBall > 0f) {
                float ballR = mix(dp(18), Math.min(w * .18f, dp(72)), becomeBall);
                p.setColor(Color.argb((int)(55 * becomeBall), 70, 132, 160));
                c.drawOval(new RectF(cx - ballR * .92f, groundY + ballR * .55f,
                        cx + ballR * .92f, groundY + ballR * .92f), p);
                RadialGradient g = new RadialGradient(cx - ballR * .30f, groundY - ballR * .34f,
                        ballR * 1.42f,
                        new int[]{Color.WHITE, Color.rgb(247, 252, 255), Color.rgb(194, 225, 241)},
                        new float[]{0f, .57f, 1f}, Shader.TileMode.CLAMP);
                p.setShader(g);
                c.drawCircle(cx, groundY, ballR, p);
                p.setShader(null);
                stroke.setColor(Color.argb((int)(110 * becomeBall), 98, 161, 191));
                stroke.setStrokeWidth(dp(1.2f));
                c.drawCircle(cx, groundY, ballR - dp(.5f), stroke);

                // Tiny sparkling remnants imply the friends are still inside the ball.
                p.setColor(Color.argb((int)(150 * becomeBall), 255, 255, 255));
                for (int i = 0; i < 8; i++) {
                    double a = i * .91 + t * .25;
                    float rr = ballR * (.28f + (i % 3) * .13f);
                    c.drawCircle(cx + (float)Math.cos(a) * rr,
                            groundY + (float)Math.sin(a) * rr,
                            dp(1.3f + (i % 2) * .6f), p);
                }
            }
        }

        void drawFriendFlake(Canvas c, float x, float y, float r, int alpha, int seed) {
            stroke.setColor(Color.argb(alpha, 119, 181, 210));
            stroke.setStrokeWidth(Math.max(dp(1.3f), r * .10f));
            for (int k = 0; k < 6; k++) {
                double a = k * Math.PI / 3;
                float ex = x + (float)Math.cos(a) * r * 1.22f;
                float ey = y + (float)Math.sin(a) * r * 1.22f;
                c.drawLine(x, y, ex, ey, stroke);
            }

            RadialGradient g = new RadialGradient(x - r * .25f, y - r * .30f, r * 1.2f,
                    new int[]{Color.WHITE, Color.rgb(239, 249, 255), Color.rgb(196, 227, 241)},
                    null, Shader.TileMode.CLAMP);
            p.setShader(g);
            p.setAlpha(alpha);
            c.drawCircle(x, y, r, p);
            p.setShader(null);
            p.setAlpha(255);

            // Friendly little faces.
            p.setColor(Color.argb(alpha, 53, 83, 101));
            c.drawCircle(x - r * .27f, y - r * .14f, r * .075f, p);
            c.drawCircle(x + r * .27f, y - r * .14f, r * .075f, p);
            stroke.setColor(Color.argb(alpha, 69, 105, 124));
            stroke.setStrokeWidth(Math.max(dp(1), r * .055f));
            RectF smile = new RectF(x - r * .30f, y - r * .02f, x + r * .30f, y + r * .40f);
            c.drawArc(smile, 20, 140, false, stroke);
        }

        void drawStoryText(Canvas c, float w, float h, float t) {
            String top;
            String bottom;
            if (t < 2.35f) {
                top = "Одного зимового дня";
                bottom = "посипав перший сніг…";
            } else if (t < 4.55f) {
                top = "На землю нападали";
                bottom = "сніжинки-друзі.";
            } else if (t < 7.0f) {
                top = "Вони міцно взялися";
                bottom = "за ручки, щоб бути разом.";
            } else {
                top = "Так народилася перша куля.";
                bottom = "Тепер твоя черга ліпити сніговика!";
            }

            float cardW = Math.min(w - dp(28), dp(380));
            float cardH = dp(88);
            float left = (w - cardW) / 2f;
            RectF card = new RectF(left, h * .08f, left + cardW, h * .08f + cardH);
            p.setColor(Color.argb(218, 255, 255, 255));
            c.drawRoundRect(card, dp(24), dp(24), p);

            text.setTextAlign(Paint.Align.CENTER);
            text.setColor(Color.rgb(34, 75, 101));
            text.setTextSize(tx(w < dp(360) ? 15f : 17f));
            c.drawText(top, card.centerX(), card.top + dp(34), text);
            text.setTextSize(tx(w < dp(360) ? 11f : 12.5f));
            text.setColor(Color.rgb(79, 126, 151));
            c.drawText(bottom, card.centerX(), card.top + dp(61), text);
        }

        void drawSkip(Canvas c, float w) {
            float top = dp(10);
            skipRect.set(w - dp(104), top, w - dp(10), top + dp(38));
            p.setColor(Color.argb(150, 255, 255, 255));
            c.drawRoundRect(skipRect, dp(16), dp(16), p);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(tx(9));
            text.setColor(Color.rgb(74, 119, 144));
            c.drawText("ПРОПУСТИТИ", skipRect.centerX(), skipRect.centerY() + dp(3.5f), text);
        }

        void drawStart(Canvas c, float w, float h, float t) {
            float pulse = .96f + (float)Math.sin(t * 4f) * .025f;
            float bw = Math.min(w - dp(42), dp(330)) * pulse;
            float bh = dp(58) * pulse;
            float cx = w / 2f;
            float cy = h - dp(72);
            startRect.set(cx - bw / 2f, cy - bh / 2f, cx + bw / 2f, cy + bh / 2f);
            p.setColor(Color.rgb(38, 105, 145));
            c.drawRoundRect(startRect, dp(20), dp(20), p);
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(tx(12.5f));
            text.setColor(Color.WHITE);
            c.drawText("ПОЧАТИ ЛІПИТИ", startRect.centerX(), startRect.centerY() + dp(4.5f), text);
        }

        @Override public boolean onTouchEvent(MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_UP) {
                performClick();
                float t = (SystemClock.elapsedRealtime() - storyStart) / 1000f;
                if (skipRect.contains(e.getX(), e.getY()) || (t >= 7f && startRect.contains(e.getX(), e.getY()))) {
                    startGame();
                }
                return true;
            }
            return true;
        }

        @Override public boolean performClick() {
            super.performClick();
            return true;
        }

        void startGame() {
            if (leaving) return;
            leaving = true;
            Intent i = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }
    }
}
