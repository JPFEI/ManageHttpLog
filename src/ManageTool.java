

import javax.swing.*;
import javax.swing.event.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ManageTool extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel lbType;
	private JList listPath;
	private JList listSubpath;
	private JList listFileName;
	private JComboBox cmbType;
	private JTextArea txtContent;
	private JMenu menuConfig;

	private HashMap<String,HashMap<String,ArrayList<String>>> dateHostFile;
	private HashMap<String,HashMap<String,ArrayList<String>>> hostDateFile;
	private HashMap<String,HashMap<String,ArrayList<String>>> curentDateFile;
	private ArrayList<String> pathNameList;
	
	String logPath = "";
	static String infFileName = "path.inf";
	public ManageTool() {
		super("日志管理");

		this.dateHostFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		this.hostDateFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		this.pathNameList = new ArrayList<String>();
		
		JPanel contect = new JPanel(new GridLayout(4, 0));

//		p.setLayout(new GridLayout(1, 0));
		GridBagLayout layout =  new GridBagLayout();
		this.setLayout(layout);

		JPanel typeContect = new JPanel(new GridLayout(4, 0));
		JButton btn = new JButton("刷新数据");
		btn.addMouseListener(new ManageToolMouseListener());
		lbType = new JLabel("类型");
		lbType.setSize(80, 100);
		// 创建一个没有选项的组合框
		cmbType = new JComboBox();//JComboBox({"时间划分","时间划分"});
		cmbType.addItem("时间划分");
		cmbType.addItem("服务器划分");
		cmbType.addItemListener(new TypeListener());
		typeContect.add(new JPanel());
		typeContect.add(cmbType);
		typeContect.add(btn);
		// 创建列表框，并设置其选项是listPath数组中的省份
		listPath = new JList();
		// 设置列表框只能单选
		listPath.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// 在列表框中注册监听
		listPath.addListSelectionListener(new FilePathListener());

		listSubpath = new JList();
		listSubpath.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSubpath.addListSelectionListener(new FileSubpathListener());
		listFileName = new JList();
		listFileName.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listFileName.addListSelectionListener(new FileListener());

		txtContent = new JTextArea(50, 30);
		txtContent.setBackground(Color.lightGray);
		contect.setBackground(Color.lightGray);
		contect.setSize(100, 600);
		contect.add(typeContect);
		JScrollPane jsc = new JScrollPane(listPath);
		contect.add(jsc);
		jsc = new JScrollPane(listSubpath);
		contect.add(jsc);
		jsc = new JScrollPane(listFileName);
		contect.add(jsc);
//		contect.add(txtContent);
		this.add(contect);
		jsc = new JScrollPane(txtContent);
		this.add(jsc);
		this.setSize(760,600);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		this.setResizable(false);
		
		GridBagConstraints s= new GridBagConstraints();//定义一个GridBagConstraints，
        //是用来控制添加进的组件的显示位置
        s.fill = GridBagConstraints.BOTH;
        //该方法是为了设置如果组件所在的区域比组件本身要大时的显示情况
        //NONE：不调整组件大小。
        //HORIZONTAL：加宽组件，使它在水平方向上填满其显示区域，但是不改变高度。
        //VERTICAL：加高组件，使它在垂直方向上填满其显示区域，但是不改变宽度。
        //BOTH：使组件完全填满其显示区域。
        s.gridwidth=1;//该方法是设置组件水平所占用的格子数，如果为0，就说明该组件是该行的最后一个
        s.weightx = 0;//该方法设置组件水平的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
        s.weighty=0;//该方法设置组件垂直的拉伸幅度，如果为0就说明不拉伸，不为0就随着窗口增大进行拉伸，0到1之间
        layout.setConstraints(contect, s);//设置组件
        s.gridwidth=6;
        s.weightx = 0.5;
        s.weighty=0.1;
        layout.setConstraints(jsc, s);
        
        JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		menuConfig = new JMenu("设置");
		JMenuItem itemConfig  = new JMenuItem("配置路径");
		menuConfig.add(itemConfig);
		menuBar.add(menuConfig);
		// 注册监听
		itemConfig.addActionListener(this);
        
        //从配置文件读取日志路径，若果不存在重新设置，否则加载数据
		File infoFile = new File(infFileName);
		if(infoFile.exists()){
	        if(getPathFromInfo() == null){
	            resetLogPath();
	        }else{
	        	reloadData();
	        }
		}else{
            resetLogPath();
		}
	}
	
	private void refishPathData(){
		Set<String> pathSet = this.curentDateFile.keySet();
		String[] pathList =  pathSet.toArray(new String[pathSet.size()]);
		listPath.setListData(pathList);
		listPath.setSelectedIndex(0);
	}
	
	public void reloadData(){
		setData4Views();
		setHostDateFile();
		this.curentDateFile = dateHostFile;//hostDateFile
		refishPathData();
	}
	
	private void resetLogPath(){

		JFileChooser fc = new JFileChooser();
		// 显示文件打开对话框
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录 
		int rVal = fc.showOpenDialog(this);
		// 如果点击确定(Yes/OK)
		if (rVal == JFileChooser.APPROVE_OPTION) {
			// 获取文件对话框中用户选中的文件名
			File chooseFile = fc.getSelectedFile();
			logPath = chooseFile.getPath();
			saveLogPathForInfo(logPath);
			reloadData();
		}else{
			// 系统退出
			System.exit(0);
		}
	}
	
	private void setData4Views(){
		File file = new File(logPath);
		this.dateHostFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		// 得到文件名列表
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for(int i=0;i<files.length;i++){
				File file1 = files[i];
				if (file1.isDirectory()) {
					File[] files1 = file1.listFiles();
					HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
					for(int j=0;j<files1.length;j++){
						File file2 = files1[j];
						ArrayList<String> fileNames = new ArrayList<String>();
						pathNameList.add(file1.getName()+"/"+file2.getName());
						if (file2.isDirectory()) {
							File[] files2 = file2.listFiles();
							for(int k=0;k<files2.length;k++){
								File file3 = files2[k];
								fileNames.add(file3.getName());
							}
							map.put(file2.getName(), fileNames);
						}
					}
					this.dateHostFile.put(file1.getName(), map);
				}
			}
		}
	}
	
	private void setHostDateFile(){
		this.hostDateFile = new HashMap<String, HashMap<String, ArrayList<String>>>();
		Set<String> hostSet = new HashSet<String>();//dateHostFile.values().keySet();//hostMap.keySet();
		Collection<HashMap<String, ArrayList<String>>> hostValuesMap = dateHostFile.values();
		for(HashMap<String, ArrayList<String>> map:hostValuesMap){
			hostSet.addAll( map.keySet());
		}
		System.out.println("pathNameList:"+pathNameList);
		Set<String> dateSet = dateHostFile.keySet();
		for(String hostName:hostSet){
			HashMap<String, ArrayList<String>> dateMap = new HashMap<String, ArrayList<String>>();
			for(String datePath:dateSet){
				HashMap<String, ArrayList<String>> hostMap = dateHostFile.get(datePath);
				if(pathNameList.contains(datePath+"/"+hostName)){
					dateMap.put(datePath, hostMap.get(hostName));
				}
			}
			hostDateFile.put(hostName, dateMap);
		}
	}

	//按时间，域名分组
	private void setViewData4DateHost(){
		this.curentDateFile = dateHostFile;
	}

	//按域名，时间分组
	private void setViewData4HostDate(){
		this.curentDateFile = hostDateFile;//
	}

	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		Object source = e.getSource();
		String cmd = e.getActionCommand();
		System.out.println(e.getActionCommand());
		System.out.println(source);
		if (cmd == "配置路径") {
			resetLogPath();
		}
	}
	
	private class TypeListener implements ItemListener {
		
		public void itemStateChanged(ItemEvent e) {
			// TODO Auto-generated method stub
			int i = cmbType.getSelectedIndex();
			switch (i) {
			case 0:
				setViewData4DateHost();
				break;
			case 1:
				setViewData4HostDate();
				break;
			}
			refishPathData();
		}
	}
	
	// 创建一个列表选择监听类
	private class FilePathListener implements ListSelectionListener {
		private int lastSelectedIndex = -1;
		
		public void valueChanged(ListSelectionEvent e) {
			if(listPath.getSelectedIndex() != lastSelectedIndex){
				String path = (String)listPath.getSelectedValue();
				if(path == null){
					return;
				}
				//System.out.println(path);
				//System.out.println(curentDateFile);
				HashMap<String, ArrayList<String>> map = curentDateFile.get(path);
				Set<String> pathSet = map.keySet();
				String[] pathArr =  pathSet.toArray(new String[pathSet.size()]);
				listSubpath.setListData(pathArr);
				listSubpath.setSelectedIndex(0);
				txtContent.setText("");
				lastSelectedIndex = listPath.getSelectedIndex();
			}
		}
	}

	private class FileSubpathListener implements ListSelectionListener {
		private int lastSelectedIndex = -1;
		
		public void valueChanged(ListSelectionEvent e) {
			if(listSubpath.getSelectedIndex() != lastSelectedIndex){
				String path = (String)listPath.getSelectedValue();
				String subpath = (String)listSubpath.getSelectedValue();
				HashMap<String, ArrayList<String>> map = curentDateFile.get(path);
				ArrayList<String> pathList = map.get(subpath);
				if(pathList != null){
					String[] fileArr =  pathList.toArray(new String[pathList.size()]);
					listFileName.setListData(fileArr);
				}else{
					listFileName.setListData(new String[0]);
				}
				txtContent.setText("");
				lastSelectedIndex = listSubpath.getSelectedIndex();
			}
		}
	}
	
	private class FileListener implements ListSelectionListener {
		private int lastSelectedIndex = -1;
		
		public void valueChanged(ListSelectionEvent e) {
			if(listFileName.getSelectedIndex() != lastSelectedIndex){
				String path = (String)listPath.getSelectedValue();
				String subpath = (String)listSubpath.getSelectedValue();
				String fileName = (String)listFileName.getSelectedValue();
//				System.out.println(path);
//				System.out.println(listSubpath.getSelectedValue());
				HashMap<String, ArrayList<String>> map = curentDateFile.get(path);
				ArrayList<String> pathList = map.get(subpath);
				String[] fileArr =  pathList.toArray(new String[pathList.size()]);
				listFileName.setListData(fileArr);
				if(cmbType.getSelectedIndex() == 0){
					openFile(logPath+"/"+path+"/"+subpath+"/"+fileName);
				}else{
					openFile(logPath+"/"+subpath+"/"+path+"/"+fileName);
				}
				lastSelectedIndex = listFileName.getSelectedIndex();
			}
		}
	}
	
	private void saveLogPathForInfo(String path){
		ObjectOutputStream obs = null;
		try {
			// 创建一个ObjectOutputStream的对象
			obs = new ObjectOutputStream(new FileOutputStream(infFileName));
			// 把对象写入到文件中
			obs.writeObject(path);
			obs.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				obs.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private String getPathFromInfo(){
		String logPathStr = null;
		ObjectInputStream ois = null;
		try {
			// 创建一个ObjectInputStream的对象,进行反序列化
			ois = new ObjectInputStream(new FileInputStream(infFileName));
			Object obj = ois.readObject();
			if (obj != null) {
				logPathStr = (String) obj;
//				System.out.println("配置文件：" + logPathStr);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		this.logPath = logPathStr;
		return logPathStr;
	}

	// 打开文件的方法
	private void openFile(String filePath) {
		System.out.println("filePath:" + filePath);
		try {
			// 创建一个文件输入流，用于读文件
			// FileReader fread = new FileReader(filePath);
			BufferedReader bread = new BufferedReader(new InputStreamReader(
					new FileInputStream(filePath), "UTF-8"));
			// 创建一个缓冲流
			// BufferedReader bread = new BufferedReader(fread);
			// 从文件中读一行信息
			String line = bread.readLine();
			StringBuffer buf = new StringBuffer();
			buf.append(line);
			// 循环读文件中的内容，并显示到文本域中
			while (line != null) {
				// 读下一行
				buf.append(line + "\n");
				line = bread.readLine();
			}
			txtContent.setText(buf.toString());
			bread.close();
			// fread.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//继承MouseAdapter适配器类
	private class ManageToolMouseListener implements MouseListener {

		/*public ManageToolMouseAdapter() {
			System.out.println("ManageToolMouseAdapter");
		}*/

		// 重写鼠标进入的事件处理方法
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			reloadData();
	        JButton btn = (JButton) e.getSource();
			System.out.println("reloadData");
			System.out.println(btn.getName());
		}

		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}

	/*public static void main(String args[]) {
		new ManageToolMouseAdapter();
	}*/
	public static void main(String[] args) {
		ManageTool f = new ManageTool();
		f.setVisible(true);
	}
}
