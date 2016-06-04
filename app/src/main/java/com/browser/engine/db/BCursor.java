package com.browser.engine.db;

import java.util.ArrayList;

import android.database.Cursor;

public class BCursor {
	
		private ArrayList<String> columns = new ArrayList<String>();
		private ArrayList<ArrayList<String>> rows =new ArrayList<ArrayList<String>> ();
		private int rowIndex=-1;

		
		
		public BCursor(Cursor c){
			init(c);
		}
		
		public void init(Cursor c){
			if(c!=null){
				if(columns.size()>0){
					columns.clear();
				}
				if(rows.size()>0){
					rows.clear();
				}
				for(int x=0;x<c.getColumnCount();x++){
					columns.add(c.getColumnName(x));
				}
				while(c.moveToNext()){
					ArrayList<String> row = new ArrayList<String>();
					for(int x=0;x<c.getColumnCount();x++){
						row.add(c.getString(x));
					}	
					rows.add(row);
				}
			}
		}
		
		public String getColumnName(int index){
			return columns.get(index);
		}
		
		public String getString(int columnIndex){
			return rows.get(rowIndex).get(columnIndex);
		}
		
		public int getColumnCount(){
			return columns.size();
		}
		
		public boolean moveToNext(){
			boolean n=false;
			if(rows.size()>(rowIndex+1)){
				rowIndex++;
				n=true;
			}
			return n;
		}
		
		public boolean isLast(){
			boolean n=false;
			if(rows.size()==(rowIndex+1)){
				n=true;
			}
			return n;
		}
		
		public int getCount(){
			return rows.size();
		}

}
