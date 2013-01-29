//
//  HomeTableCellControllerCell.m
//  PanoPlayer
//
//  Created by 李文博 on 13-1-18.
//  Copyright (c) 2013年 __MyCompanyName__. All rights reserved.
//

#import "HomeTableCellControllerCell.h"

@implementation HomeTableCellControllerCell

@synthesize thumbImage;
@synthesize panoTitle;
@synthesize photoTime;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

@end
