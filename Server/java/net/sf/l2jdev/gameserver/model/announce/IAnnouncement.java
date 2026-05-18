package net.sf.l2jdev.gameserver.model.announce;

public interface IAnnouncement
{
	int getId();

	AnnouncementType getType();

	void setType(AnnouncementType var1);

	boolean isValid();

	String getContent();

	void setContent(String var1);

	String getAuthor();

	void setAuthor(String var1);

	boolean storeMe();

	boolean updateMe();

	boolean deleteMe();
}
