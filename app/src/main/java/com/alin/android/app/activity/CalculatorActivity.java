package com.alin.android.app.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.alin.android.app.common.BaseAppActivity;
import com.alin.android.app.R;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author: Create By ZhangWenLin
 * @create: 2018-11-09 11:03
 **/
public class CalculatorActivity extends BaseAppActivity implements View.OnClickListener{

    private Context context;
    private TextView calTextTop;
    private TextView calTextBottom;
    private TextView one;
    private TextView two;
    private TextView three;
    private TextView four;
    private TextView five;
    private TextView six;
    private TextView seven;
    private TextView eight;
    private TextView nine;
    private TextView zero;
    private TextView clear;
    private TextView delete;
    private TextView remain;
    private TextView divider;
    private TextView multi;
    private TextView plus;
    private TextView minus;
    private TextView dot;
    private TextView equal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator_activity);
        initView();
    }

    private void initView(){
        context = CalculatorActivity.this;
        calTextTop = (TextView) findViewById(R.id.calculate_text_top);
        calTextBottom = (TextView) findViewById(R.id.calculate_text_bottom);
        one = (TextView) findViewById(R.id.calculate_one);
        two = (TextView) findViewById(R.id.calculate_two);
        three = (TextView) findViewById(R.id.calculate_three);
        four = (TextView) findViewById(R.id.calculate_four);
        five = (TextView) findViewById(R.id.calculate_five);
        six = (TextView) findViewById(R.id.calculate_six);
        seven = (TextView) findViewById(R.id.calculate_seven);
        eight = (TextView) findViewById(R.id.calculate_eight);
        nine = (TextView) findViewById(R.id.calculate_nine);
        zero = (TextView) findViewById(R.id.calculate_zero);
        clear = (TextView) findViewById(R.id.calculate_clear);
        delete = (TextView) findViewById(R.id.calculate_delete);
        remain = (TextView) findViewById(R.id.calculate_remain);
        divider = (TextView) findViewById(R.id.calculate_divider);
        multi = (TextView) findViewById(R.id.calculate_multi);
        plus = (TextView) findViewById(R.id.calculate_plus);
        minus = (TextView) findViewById(R.id.calculate_minus);
        dot = (TextView) findViewById(R.id.calculate_dot);
        equal = (TextView) findViewById(R.id.calculate_equal);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        five.setOnClickListener(this);
        six.setOnClickListener(this);
        seven.setOnClickListener(this);
        eight.setOnClickListener(this);
        nine.setOnClickListener(this);
        zero.setOnClickListener(this);
        clear.setOnClickListener(this);
        delete.setOnClickListener(this);
        remain.setOnClickListener(this);
        divider.setOnClickListener(this);
        multi.setOnClickListener(this);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        dot.setOnClickListener(this);
        equal.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        TextView tv = (TextView) view;
        String text = calTextBottom.getText().toString();
        switch (view.getId()){
            case R.id.calculate_one:
            case R.id.calculate_two:
            case R.id.calculate_three:
            case R.id.calculate_four:
            case R.id.calculate_five:
            case R.id.calculate_six:
            case R.id.calculate_seven:
            case R.id.calculate_eight:
            case R.id.calculate_nine:
            case R.id.calculate_zero:
                calTextBottom.setText(text+tv.getText().toString()+"");
                break;
            case R.id.calculate_dot:
                if (checkChar()){
                    calTextBottom.setText(text+tv.getText().toString()+"");
                }
                break;
            case R.id.calculate_clear:
                calTextTop.setText("");
                calTextBottom.setText("");
                break;
            case R.id.calculate_delete:
                if (!"".equals(calTextBottom.getText().toString())){
                    calTextBottom.setText(text.substring(0, calTextBottom.getText().length()-1));
                }
                break;
            case R.id.calculate_plus:
            case R.id.calculate_minus:
            case R.id.calculate_multi:
            case R.id.calculate_divider:
            case R.id.calculate_remain:
                if (checkChar()){
                    if (!text.contains("+")
                            && !text.contains("-")
                            && !text.contains("×")
                            && !text.contains("÷")
                            && !text.contains("%")){
                        calTextBottom.setText(text+" "+tv.getText().toString()+" ");
                    } else {
                        getResult();
                    }
                }
                break;
            case R.id.calculate_equal:
                getResult();
                break;
            default:
                break;
        }
    }

    private boolean checkChar(){
        String cs = calTextBottom.getText().toString();
        return !"".equals(cs) && !" ".equals(cs.substring(cs.length() - 1)) && !".".equals(cs.substring(cs.length() - 1));
    }

    private void getResult(){
        String cs = calTextBottom.getText().toString();
        calTextTop.setText(cs);
        if (checkChar() && cs.contains(" ")){
            List<String> cl = Arrays.asList(StringUtils.split(cs, " "));
            Double forward = Double.parseDouble(cl.get(0));
            Double back = Double.parseDouble(cl.get(2));
            Double result = null;
            if ("+".equals(cl.get(1))){
                result = forward+back;
            }
            if ("-".equals(cl.get(1))){
                result = forward-back;
            }
            if ("×".equals(cl.get(1))){
                result = forward*back;
            }
            if ("÷".equals(cl.get(1))){
                result = "0".equals(back.toString())?0:forward/back;
            }
            if ("%".equals(cl.get(1))){
                result = forward%back;
            }
            calTextBottom.setText(result==null?"":result.toString());
        }else{
            calTextBottom.setText("");
        }
    }
}
