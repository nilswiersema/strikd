package strikd.game.achievements;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity @Table(name="achievements")
public class Achievement
{
	@Id
	private int id;
	
	@Column(nullable=false)
	private String code;
	
	@Column(nullable=false)
	private int maxProgress;
	
	public Achievement(int id, String code)
	{
		this(id, code, 1);
	}
	
	public Achievement(int id, String code, int maxProgress)
	{
		this.id = id;
		this.code = code;
		this.maxProgress = maxProgress;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getCode()
	{
		return this.code;
	}
	
	public int getMaxProgress()
	{
		return this.maxProgress;
	}
}
