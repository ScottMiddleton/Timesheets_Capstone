package com.example.scott.timesheets_capstone.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.scott.timesheets_capstone.R;
import com.example.scott.timesheets_capstone.model.Contract;

import java.util.ArrayList;

public class ContractsListAdapter
        extends RecyclerView.Adapter {

    private TextView mCompanyNameTv;
    private TextView mStartDateTv;
    private ArrayList<Contract> mContractsArray;
    private ContractOnClickHandler mContractClickHandler;
    private EditButtonOnClickHandler mEditButtonClickHandler;
    private ImageView mEditContractButton;

    public ContractsListAdapter(Context context,
                                ContractOnClickHandler mContractClickHandler,
                                EditButtonOnClickHandler mEditButtonClickHandler ) {
        Context mContext = context;
        this.mContractClickHandler = mContractClickHandler;
        this.mEditButtonClickHandler = mEditButtonClickHandler;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View view) {
            super(view);
            mCompanyNameTv = view.findViewById(R.id.company_name_tv);
            mStartDateTv = view.findViewById(R.id.start_date_tv);

            mEditContractButton = view.findViewById(R.id.edit_contract_button);

            mEditContractButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int adapterPosition = getAdapterPosition();
                            Contract currentContract = mContractsArray.get(adapterPosition);
                            mEditButtonClickHandler.onEditClick(currentContract);
                        }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    Contract currentContract = mContractsArray.get(adapterPosition);
                    mContractClickHandler.onClick(currentContract);
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.contract_list_content;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        mCompanyNameTv.setText(mContractsArray.get(position).getCompanyName());
        mStartDateTv.setText(mContractsArray.get(position).getStartDate());
        holder.itemView.setTag(mContractsArray.get(position));
    }

    @Override
    public int getItemCount() {
        if (mContractsArray != null) {
            return mContractsArray.size();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setAdapterData(ArrayList<Contract> data) {
        mContractsArray = data;
        notifyDataSetChanged();
    }

    public interface ContractOnClickHandler {
        void onClick(Contract contract);

    }

    public interface EditButtonOnClickHandler {
        void onEditClick(Contract contract);
    }

}



