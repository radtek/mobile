package com.futureconcepts.ax.model.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class Bureau extends AddressedTable
{
    public static final String NAME = "Name";
    public static final String CONTACT_NAME = "ContactName";
    public static final String TOOLTIP = "ToolTip";
    public static final String ABBR = "Abbr";
    public static final String NUMBER = "Number";
    public static final String PHONE = "Phone";
    public static final String DIVISION = "Division";
    public static final String SORT = "Sort";

	/**
     * The content:// style URL for this table
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/Bureau");

    /**
     * The MIME type of {@link #CONTENT_URI} providing a directory of notes.
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.futureconcepts.Bureau";

    /**
     * The MIME type of a {@link #CONTENT_URI} sub-directory of a single note.
     */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.futureconcepts.Bureau";

	public Bureau(Context context, Cursor cursor)
	{
		super(context, cursor);
	}

	@Override
	public void close()
	{
		super.close();
	}

	@Override
	public Uri getContentUri()
	{
		return CONTENT_URI;
	}

	public String getTooltip()
	{
		return getCursorString(TOOLTIP);
	}

	public String getABBR()
	{
		return getCursorString(ABBR);
	}	
	
	public String getName()
	{
		return getCursorString(NAME);
	}
	
	public int getNumber()
	{
		return getCursorInt(NUMBER);
	}
	
	public int getSort()
	{
		return getCursorInt(SORT);
	}
		
	public static Bureau query(Context context, Uri uri)
	{
		Bureau result = null;
		try
		{
			result = new Bureau(context, context.getContentResolver().query(uri, null, null, null, null));
			if (result.getCount() == 1)
			{
				result.moveToFirst();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
}
