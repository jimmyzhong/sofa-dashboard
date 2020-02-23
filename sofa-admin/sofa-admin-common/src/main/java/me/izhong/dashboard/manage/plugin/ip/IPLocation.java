package me.izhong.dashboard.manage.plugin.ip;

import lombok.extern.slf4j.Slf4j;
import me.izhong.dashboard.manage.constants.Global;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ip定位(使用byte数据方式读取)
 */
@Slf4j
public class IPLocation {
	
	private  byte[] data;
	
	private  long firstIndexOffset;

	private  long lastIndexOffset;

	private  long totalIndexCount;
	
	private static final byte REDIRECT_MODE_1 = 0x01;
	
	private static final byte REDIRECT_MODE_2 = 0x02;
	
	static   final long IP_RECORD_LENGTH = 7;
	
	private static ReentrantLock lock = new ReentrantLock();

	private static Long lastModifyTime = 0L;

	public static boolean enableFileWatch = false;

//	private File qqwryFile;

//	private String configFilePath;

	private static volatile Map<String,Boolean> isLoadMap = new HashMap<>();

	private IPLocation() {

	}

	private static IPLocation instance;

	public static Location fetchIP(String ip) throws Exception{
		if(instance == null) {
			synchronized (IPLocation.class) {
				if(instance == null) {
					IPLocation ips = new IPLocation();
					String ipPath = Global.getIpFilePath();
					ips.load(ipPath);
					instance = ips;
				}
			}
		}
		return instance.fetchIPLocation(ip);
	}

	public static Location fetchIP(String configFilePath, String ip) throws Exception{
		if(instance == null) {
			synchronized (IPLocation.class) {
				if(instance == null) {
					IPLocation ips = new IPLocation();
					ips.load(configFilePath);
					instance = ips;
				}
			}
		}
		return instance.fetchIPLocation(ip);
	}
//    private void watch() {
//    	Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
//			@Override
//			public void run() {
//				long time = qqwryFile.lastModified();
//				if (time > lastModifyTime) {
//					lastModifyTime = time;
//					try {
//						load();
//						System.out.println("reload");
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}, 1000L, 5000L, TimeUnit.MILLISECONDS);
//    }
    
	private void load(String configFilePath) throws Exception {
		//lastModifyTime = qqwryFile.lastModified();
		ByteArrayOutputStream out = null;
		InputStream in = null;

//		Boolean isLoad = isLoadMap.get(filePath);
//		if(isLoad != null && isLoad.booleanValue()) {
//			return;
//		}

		try {
//			lock.lock();
//
//			isLoad = isLoadMap.get(filePath);
//			if(isLoad == null || !isLoad.booleanValue()) {
//				this.configFilePath = filePath;
//				qqwryFile = new File(filePath);
//				isLoadMap.put(filePath,Boolean.TRUE);
//			} else {
//				return;
//			}

			out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			File qqwryFile = new File(configFilePath);
			if (qqwryFile.exists()) {
				in = new FileInputStream(qqwryFile);
			} else {
				log.info("查找文件不存在:{},使用CLASSPATH查找" , configFilePath);

				ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
				Resource[] resources = resolver.getResources(configFilePath);
				Resource res = resources[0];
				in = res.getInputStream();

				File tmpFile = File.createTempFile("ipdata","data.dat");
				tmpFile.deleteOnExit();
				StreamUtils.copy(in,new FileOutputStream(tmpFile));
				in.close();

				in = new FileInputStream(tmpFile);
				if (in == null) {
					log.info("CLASSPATH查找文件不存在:" + configFilePath);
				}
			}
			while(in.read(b) != -1){
				out.write(b);
			}
			data = out.toByteArray();
			firstIndexOffset = read4ByteAsLong(0);
			lastIndexOffset = read4ByteAsLong(4);
			totalIndexCount = (lastIndexOffset - firstIndexOffset) / IP_RECORD_LENGTH + 1;
			in.close();
			out.close();
		} finally {
			try {
				if(out != null) {
					out.close();
				}
				if(in != null) {
					in.close();
				}
			} catch (IOException e) {
				log.error("加载ip配置文件异常",e);
			}
			//lock.unlock();
		}
	}
	
	private long read4ByteAsLong(final int  offset) {
		long val = data[offset] & 0xFF;
		val |= (data[offset + 1] << 8L) & 0xFF00L;
		val |= (data[offset + 2] << 16L) & 0xFF0000L;
		val |= (data[offset + 3] << 24L) & 0xFF000000L;
		return val;
	}

	private long read3ByteAsLong(final int offset) {
		long val = data[offset] & 0xFF;
		val |= (data[offset + 1] << 8) & 0xFF00;
		val |= (data[offset + 2] << 16) & 0xFF0000;
		return val;
	}
    
	private long search(long ip) {
		long low = 0;
		long high = totalIndexCount;
		long mid = 0;
		while(low <= high){
			mid = (low + high) >>> 1 ;
		    long indexIP = read4ByteAsLong((int)(firstIndexOffset + (mid - 1) * IP_RECORD_LENGTH));
	        long indexIPNext = read4ByteAsLong((int)(firstIndexOffset + mid * IP_RECORD_LENGTH));
		    if(indexIP <= ip && ip < indexIPNext) {
		    	return read3ByteAsLong((int)(firstIndexOffset + (mid - 1) * IP_RECORD_LENGTH + 4));
		    } else {
		    	if(ip > indexIP) {
				    low = mid + 1;
				} else if (ip < indexIP) {
				    high = mid - 1;
				}
		    }
		}
		return -1;
	}
	
	public Location fetchIPLocation(String ip) {
		if(data == null) {
			log.info("ip数据库还没有初始化，{}返回空",ip);
			return null;
		}
		long numericIp = inet_pton(ip);
		long offset = search(numericIp);
		try{
			if(offset != -1) {
				return readIPLocation((int)offset);
			}
		} finally {
		}
		return null;
	}
	
	private Location readIPLocation(final int offset) {
		final Location loc = new Location();
		try {
			byte redirectMode = data[offset + 4];
			if (redirectMode == REDIRECT_MODE_1) {
				long countryOffset = read3ByteAsLong((int)offset + 5);
				redirectMode = data[(int)countryOffset];
				if (redirectMode == REDIRECT_MODE_2) {
					final QQwryString country = readString((int)read3ByteAsLong((int)countryOffset + 1));
					loc.country = country.string;
					countryOffset = countryOffset + 4;
				} else {
					final QQwryString country = readString((int)countryOffset);
					loc.country = country.string;
					countryOffset += country.byteCountWithEnd;
				}
				loc.area = readArea((int)countryOffset);
			} else if (redirectMode == REDIRECT_MODE_2) {
				loc.country = readString((int)read3ByteAsLong((int)offset + 5)).string;
				loc.area = readArea((int)offset + 8);
			} else {
				final QQwryString country = readString((int)offset + 4);
				loc.country = country.string;
				loc.area = readArea((int)offset + 4 + country.byteCountWithEnd);
			}
			return loc;
		} catch (Exception e) {
			return null;
		}
	}

	private String readArea(final int offset) {
		byte redirectMode = data[offset];
		if (redirectMode == REDIRECT_MODE_1 || redirectMode == REDIRECT_MODE_2) {
			long areaOffset = read3ByteAsLong((int)offset + 1);
			if (areaOffset == 0) {
				return "";
			} else {
				return readString((int)areaOffset).string;
			}
		} else {
			return readString(offset).string;
		}
	}
	
	private QQwryString readString(int offset) {
		int pos = offset;
		final byte[] b = new byte[128];
		int i;
		for (i = 0, b[i] = data[pos++]; b[i] != 0; b[++i] = data[pos++]);
		try{
			   return new QQwryString(new String(b,0,i,"GBK"),i + 1);
		} catch(UnsupportedEncodingException e) {
			return new QQwryString("",0);
		}
	}
	
	 /**
     * @Description:“.”号分隔的字符串转换为long类型的数字
     * @param ipStr 
     * @return:long
     */
	private static long inet_pton(String ipStr) {
		if(ipStr == null){
			throw new NullPointerException("ip不能为空");
		}
		String [] arr = ipStr.split("\\.");
		long ip = (Long.parseLong(arr[0])  & 0xFFL) << 24 & 0xFF000000L;
		ip |=  (Long.parseLong(arr[1])  & 0xFFL) << 16 & 0xFF0000L;
		ip |=  (Long.parseLong(arr[2])  & 0xFFL) << 8 & 0xFF00L;
		ip |=  (Long.parseLong(arr[3])  & 0xFFL);
		return ip;
	}
	
	private class QQwryString{
		
		public final String string;
		
		public final int byteCountWithEnd;
		
		public QQwryString(final String string,final int byteCountWithEnd) {
			this.string = string;
			this.byteCountWithEnd = byteCountWithEnd;
		}
		
		@Override
		public String toString() {
			return string;
		}
		
	}
}

