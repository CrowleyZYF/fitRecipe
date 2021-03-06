package cn.fitrecipe.android.Adpater;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.fitrecipe.android.R;
import cn.fitrecipe.android.entity.PlanComponent;

/**
 * Created by wk on 2015/8/28.
 */
public class IngredientAdapter extends BaseAdapter{

    private Context context;
    private List<PlanComponent> componentList;
    private List<PlanComponent> basket;

    public IngredientAdapter(Context context, List<PlanComponent> componentList, List<PlanComponent> basket){
        this.context = context;
        this.componentList = componentList;
        this.basket = basket;
    }

    @Override
    public int getCount() {
        if(componentList == null)   return 0;
        return componentList.size();
    }

    @Override
    public Object getItem(int position) {
        return componentList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        }
        else {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.ingredient_list_item, null);
            holder.textView1 = (TextView) convertView.findViewById(R.id.ingredient_name);
            holder.textView2 = (TextView) convertView.findViewById(R.id.ingredient_weight);
            holder.imageView = (ImageView) convertView.findViewById(R.id.ingredient_line);
            convertView.setTag(holder);
        }
        holder.textView1.setText(componentList.get(position).getName());
        holder.textView2.setText(componentList.get(position).getAmount()+"g");
        final int status = componentList.get(position).getStatus();
        if(status == 0) {
            holder.textView1.setTextColor(this.context.getResources().getColor(R.color.login_input_text_color));
            holder.textView2.setTextColor(this.context.getResources().getColor(R.color.login_input_text_color));
            holder.imageView.setVisibility(View.INVISIBLE);
        }else {
            holder.textView1.setTextColor(this.context.getResources().getColor(R.color.gray));
            holder.textView2.setTextColor(this.context.getResources().getColor(R.color.gray));
            holder.imageView.setVisibility(View.VISIBLE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                componentList.get(position).setStatus(1 - status);
                if(basket != null){
                    for(int i = 0; i <basket.size(); i++) {
                        PlanComponent pcomponent = basket.get(i);
                        if(pcomponent.getType() == 1) {
                            for (int j = 0; j < pcomponent.getComponents().size(); j++) {
                                PlanComponent component = pcomponent.getComponents().get(j);
                                if (component.getName().equals(componentList.get(position).getName()))
                                    component.setStatus(1 - status);
                            }
                        }else {
                            if (pcomponent.getName().equals(componentList.get(position).getName()))
                                pcomponent.setStatus(1 - status);
                        }
                    }
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView textView1;
        TextView textView2;
        ImageView imageView;
    }

}
