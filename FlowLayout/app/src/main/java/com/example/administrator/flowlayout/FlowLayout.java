package com.example.administrator.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/8.
 */

public class FlowLayout extends ViewGroup {

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    /**
     * 负责设置子控件的测量模式和大小 根据所有子控件设置自己的宽和高
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //获取父容器设置的测量模式和宽高
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //如果设置为wrap_content，则使用以下宽高
        int width = 0;
        int height = 0;

        //每一行的宽度
        int lineWidth = 0;

        //每一行的高度
        int lineHeight = 0;

        //子View的个数
        int cCount = getChildCount();

        for(int i = 0; i < cCount ; i++){
            View childView = getChildAt(i);
            //测量出子View的宽高
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            //当前子View所占宽度
            int childViewWidth = childView.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin;
            //当前子View所占高度
            int childViewHeight = childView.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;

            //如果加入当前childView后超出了该行最大宽度，则把到目前为止最大的宽度赋值给width，累加height，然后开启新的一行
            if(lineWidth + childViewWidth > sizeWidth){
                width = Math.max(lineWidth, childViewWidth);
                height += lineHeight;
                lineWidth = childViewWidth;
                lineHeight = childViewHeight;
            }else{
                //如果加入当前的childView不超过该行的最大宽度，则继续往后添加，再判断行高是否是最大
                lineWidth += childViewWidth;
                lineHeight = Math.max(lineHeight, childViewHeight);
            }

            //最后一个childView
            if(i == cCount - 1){
                width = Math.max(width, lineWidth);
                height += lineHeight;
            }
        }
        setMeasuredDimension(modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width, modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height);
    }

    /**
     * 存储所有的View，按行记录
     */
    private List<List<View>> allViews = new ArrayList<List<View>>();

    /**
     * 记录每一行的最大高度
     */
    private List<Integer> allLineHeight = new ArrayList<Integer>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        allViews.clear();
        allLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();

        for(int i = 0; i < cCount; i++){
            View childView = getChildAt(i);
            MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
            int childViewWidth = childView.getMeasuredWidth();
            int childViewHeight = childView.getMeasuredHeight();

            //如果需要换行
            if(lineWidth + childViewWidth + layoutParams.leftMargin + layoutParams.rightMargin > width){
                allLineHeight.add(lineHeight);
                allViews.add(lineViews);
                lineWidth = 0;
                lineViews = new ArrayList<>();
            }

            lineHeight = Math.max(lineHeight, childViewHeight + layoutParams.topMargin + layoutParams.bottomMargin);
            lineWidth += childViewWidth + layoutParams.leftMargin + layoutParams.rightMargin;
            lineViews.add(childView);
        }

        //记录最后一行
        allViews.add(lineViews);
        allLineHeight.add(lineHeight);

        int left = 0;
        int top = 0;

        //总行数
        int lineNums = allViews.size();

        //按每一行进行布局
        for(int i = 0; i < lineNums; i++){
            lineViews = allViews.get(i);
            lineHeight = allLineHeight.get(i);

            //按每一行中的每一个view进行布局
            for(int j = 0; j < lineViews.size(); j++){
                View childView = lineViews.get(j);
                if (childView.getVisibility() == View.GONE)
                {
                    continue;
                }
                MarginLayoutParams layoutParams = (MarginLayoutParams) childView.getLayoutParams();
                int lc = left + layoutParams.leftMargin;
                int tc = top + layoutParams.topMargin;
                int rc = lc + childView.getMeasuredWidth();
                int bc = tc + childView.getMeasuredHeight();

                childView.layout(lc, tc, rc, bc);

                //按照每一行追加的view进行X坐标的叠加计算。
                left += layoutParams.leftMargin + layoutParams.rightMargin + childView.getMeasuredWidth();
            }
            //一行结束，距离左边重置为0，Y坐标添加到下一行的Y坐标
            left = 0;
            top += lineHeight;
        }
    }
}
