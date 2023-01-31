// /*
//  * Copyright (C) 2020 Google Inc.
//  *
//  * Licensed under the Apache License, Version 2.0 (the "License");
//  * you may not use this file except in compliance with the License.
//  * You may obtain a copy of the License at
//  *
//  *      http://www.apache.org/licenses/LICENSE-2.0
//  *
//  * Unless required by applicable law or agreed to in writing, software
//  * distributed under the License is distributed on an "AS IS" BASIS,
//  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  * See the License for the specific language governing permissions and
//  * limitations under the License.
//  */

 package com.google.gapid.perfetto.views;

 import static com.google.gapid.widgets.Widgets.createTableViewer;
 import static com.google.gapid.widgets.Widgets.packColumns;
 
 import com.google.gapid.models.Analytics;
 import com.google.gapid.models.Perfetto;
 import com.google.gapid.perfetto.QueryViewer.ResultContentProvider;
 import com.google.gapid.perfetto.QueryViewer.Row;
 import com.google.gapid.proto.perfetto.Perfetto.QueryResult;
 import com.google.gapid.proto.service.Service.ClientAction;
 import com.google.gapid.rpc.Rpc;
 import com.google.gapid.rpc.Rpc.Result;
 import com.google.gapid.rpc.RpcException;
 import com.google.gapid.rpc.UiCallback;
 import com.google.gapid.util.Messages;
 import com.google.gapid.widgets.DialogBase;
 import com.google.gapid.widgets.Theme;
 import com.google.gapid.widgets.Widgets;
 import java.util.concurrent.ExecutionException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
//  import org.eclipse.swt.widgets.TableColumn;
 

//  public class ZoomScale {

//   Shell shell = new Shell(display);

//   Scale scale;
//   Text value;
  
//   public ZoomScale(Shell shell, Theme theme) {
//     Composite area = (Composite) super.cre
//     Display display = new Display();
//     shell.setLayout(new GridLayout(1, true));
    
//     Label label = new Label(shell, SWT.NULL);
//     label.setText("Volume:");
    
//     scale = new Scale(shell, SWT.VERTICAL);
//     scale.setBounds(0, 0, 40, 200);
//     scale.setMaximum(20);
//     scale.setMinimum(0);
//     scale.setIncrement(1);
//     scale.setPageIncrement(5);
    
//     scale.addListener(SWT.Selection, new Listener() {
//       public void handleEvent(Event event) {
//         int perspectiveValue = scale.getMaximum() - scale.getSelection() + scale.getMinimum();
//         value.setText("Vol: " + perspectiveValue);
//       }
//     });
    
//     value = new Text(shell, SWT.BORDER | SWT.SINGLE);

//     value.setEditable(false);
//     scale.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
//     value.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

//     shell.pack();
//     shell.open();
//     //textUser.forceFocus();

//     // Set up the event loop.
//     while (!shell.isDisposed()) {
//       if (!display.readAndDispatch()) {
//         // If no more entries in event queue
//         display.sleep();
//       }
//     }

//     display.dispose();
//   }
// }
 public class ZoomScale {
 
   private static final Logger LOG = Logger.getLogger(ZoomScale.class.getName());
   private static final String QUERY_STR =
       "SELECT name AS 'Name', CAST(COALESCE(int_value, str_value, 'NULL') as TEXT) as 'Value' FROM"
           + " metadata;";
 
   public static void showMetadata(
       Shell shell, Theme theme) {
     new DialogBase(shell, theme) {
       @Override
       public String getTitle() {
         return Messages.ZOOM_SCALE_WINDOW_TITLE;
       }
 
       @Override
       protected Control createDialogArea(Composite parent) {
         Composite area = (Composite) super.createDialogArea(parent);
         TableViewer table = createTableViewer(area, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
         table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
         table.setContentProvider(new ResultContentProvider());
         table.setLabelProvider(new LabelProvider());
         Rpc.listen(
             perfetto.query(QUERY_STR),
             new UiCallback<QueryResult, QueryResult>(area, LOG) {
               @Override
               protected QueryResult onRpcThread(Result<QueryResult> result)
                   throws ExecutionException {
                 try {
                   return result.get();
                 } catch (RpcException e) {
                   LOG.log(Level.WARNING, "System Profile Query failure", e);
                   return QueryResult.newBuilder().setError(e.toString()).build();
                 }
               }
 
               @Override
               protected void onUiThread(QueryResult result) {
                 table.setInput(null);
                 for (TableColumn col : table.getTable().getColumns()) {
                   col.dispose();
                 }
 
                 if (!result.getError().isEmpty()) {
                   Widgets.createLabel(area, "Error: " + result.getError());
                   area.requestLayout();
                 } else if (result.getNumRecords() == 0) {
                   Widgets.createLabel(area, "Query returned no rows.");
                   area.requestLayout();
                 } else {
                   for (int i = 0; i < result.getColumnDescriptorsCount(); i++) {
                     int col = i;
                     QueryResult.ColumnDesc desc = result.getColumnDescriptors(i);
                     Widgets.createTableColumn(
                         table, desc.getName(), row -> ((Row) row).getValue(col));
                     table.setInput(result);
                     packColumns(table.getTable());
                     table.getTable().requestLayout();
                   }
                 }
               }
             });
         return area;
       }
 
       @Override
       protected void createButtonsForButtonBar(Composite parent) {
         createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
       }
     }.open();
   }
 }
 